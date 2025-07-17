package com.ddoongs.auth.domain.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.ServiceTestSupport;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberNotFoundException;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.member.Password;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.member.PasswordMismatchException;
import com.ddoongs.auth.domain.member.RegisterMember;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationService;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Import(AuthTestConfiguration.class)
@SpringBootTest(classes = TestApplication.class)
class TokenServiceTest {

  @Autowired
  private TokenService tokenService;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private BlacklistTokenRepository blacklistTokenRepository;

  @Autowired
  private TokenIssuer tokenIssuer;

  @Autowired
  private TokenProvider tokenProvider;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private VerificationService verificationService;

  @Autowired
  private MemberService memberService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @DisplayName("로그인을 할 수 있다.")
  @Test
  void login() {
    String email = "test@test.com";
    String password = "123qwe!@#";
    Verification verification = ServiceTestSupport.prepareRegister(verificationService, email);
    memberService.register(new RegisterMember(email, password), verification.getId());

    TokenPair tokenPair = tokenService.login(new LoginMember(email, password));

    assertThat(tokenPair.accessToken()).isNotNull();
    assertThat(tokenPair.refreshToken()).isNotNull();

    String jti = tokenProvider.extractJti(tokenPair.refreshToken().token());

    assertThat(refreshTokenRepository.find(jti)).isPresent();
  }

  @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다.")
  @Test
  void loginFailDifferentPassword() {
    String email = "test@test.com";
    String password = "123qwe!@#";
    Verification verification = ServiceTestSupport.prepareRegister(verificationService, email);
    memberService.register(new RegisterMember(email, password), verification.getId());

    assertThatThrownBy(() -> tokenService.login(new LoginMember(email, "456rty$%^")))
        .isInstanceOf(PasswordMismatchException.class);
  }

  @DisplayName("회원 정보가 존재하지 않으면 로그인에 실패한다.")
  @Test
  void loginFailNotExistEmail() {
    assertThatThrownBy(() -> tokenService.login(new LoginMember("test@test.com", "456rty$%^")))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @Test
  @DisplayName("유효한 리프레시 토큰으로 재발급 시 새로운 토큰 쌍을 발급한다")
  void reissue_withValidToken_issuesNewTokenPair() {
    // given
    Member member = memberRepository.save(
        new Member(null, new Email("test@example.com"), new Password("password"), null));
    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);

    // when
    TokenPair tokenPair = tokenService.reissue(refreshToken.token());

    // then
    assertThat(tokenPair.accessToken()).isNotNull();
    assertThat(tokenPair.refreshToken()).isNotNull();

    String oldJti = tokenProvider.extractJti(refreshToken.token());
    String newJti = tokenProvider.extractJti(tokenPair.refreshToken().token());

    assertThat(refreshTokenRepository.find(oldJti)).isEmpty();
    assertThat(blacklistTokenRepository.exists(oldJti)).isTrue();
    assertThat(refreshTokenRepository.find(newJti)).isPresent();
  }

  @Test
  @DisplayName("블랙리스트에 등록된 토큰으로 재발급 시 reissue가 실패한다.")
  void reissue_withBlacklistedToken_throwsInvalidTokenException() {
    // given
    String jti = "blacklisted-jti";
    String blacklistedToken = "blacklisted-token";

    blacklistTokenRepository.save(jti, Duration.ofSeconds(1000L));

    // when & then
    assertThatThrownBy(() -> tokenService.reissue(blacklistedToken))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("존재하지 않는 리프레시 토큰으로 재발급 시 reissue가 실패한다.")
  void reissue_withNonExistentToken_throwsInvalidTokenException() {
    // given
    String nonExistentToken = "non-existent-token";

    // when & then
    assertThatThrownBy(() -> tokenService.reissue(nonExistentToken))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("토큰에 해당하는 사용자를 찾을 수 없으면 reissue는 실패한다.")
  void reissue_withNonExistentMember_throwsMemberNotFoundException() {
    // given
    Member member =
        new Member(999L, new Email("ghost@example.com"), new Password("password"), null);
    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);

    // when & then
    assertThatThrownBy(() -> tokenService.reissue(refreshToken.token()))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @Test
  @DisplayName("renew 불가능한 리프레시 토큰으로 renew 시 예외가 발생한다")
  void renew_withNonRenewableToken_throwsException() {
    Member member = memberRepository.save(
        new Member(null, new Email("test@example.com"), new Password("password"), null));

    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);
    refreshTokenRepository.save(refreshToken);

    assertThatThrownBy(() -> tokenService.renew(refreshToken.token()))
        .isInstanceOf(TokenRenewalConditionNotMetException.class);
  }

  @Test
  @DisplayName("블랙리스트에 등록된 토큰으로 갱신 시 예외가 발생한다")
  void renew_withBlacklistedToken_throwsInvalidTokenException() {
    // given
    String jti = "blacklisted-jti";
    String blacklistedToken = "blacklisted-token";

    blacklistTokenRepository.save(jti, Duration.ofSeconds(1000L));

    assertThatThrownBy(() -> tokenService.renew(blacklistedToken))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("존재하지 않는 리프레시 토큰으로 갱신 시 예외가 발생한다")
  void renew_withNonExistentToken_throwsInvalidTokenException() {
    // given
    String nonExistentToken = "non-existent-token";

    // when & then
    assertThatThrownBy(() -> tokenService.renew(nonExistentToken))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("갱신 가능한 리프레시 토큰으로 갱신 시 새로운 토큰 쌍을 발급한다")
  void renew_withRenewableToken_issuesNewTokenPair() {
    // given
    Member member = memberRepository.save(
        new Member(null, new Email("test@example.com"), new Password("password"), null));
    RefreshToken refreshToken = tokenProvider.createRefreshToken(member, Duration.ofSeconds(1000L));
    refreshTokenRepository.save(refreshToken);

    // when
    TokenPair tokenPair = tokenService.renew(refreshToken.token());

    // then
    assertThat(tokenPair.accessToken()).isNotNull();
    assertThat(tokenPair.refreshToken()).isNotNull();

    String oldJti = tokenProvider.extractJti(refreshToken.token());
    String newJti = tokenProvider.extractJti(tokenPair.refreshToken().token());

    assertThat(refreshTokenRepository.find(oldJti)).isEmpty();
    assertThat(blacklistTokenRepository.exists(oldJti)).isTrue();
    assertThat(refreshTokenRepository.find(newJti)).isPresent();
  }

  @Test
  @DisplayName("토큰에 해당하는 사용자를 찾을 수 없으면 갱신 시 예외가 발생한다")
  void renew_withNonExistentMember_throwsMemberNotFoundException() {
    // given
    Member member =
        new Member(999L, new Email("ghost@example.com"), new Password("password"), null);
    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);
    refreshTokenRepository.save(refreshToken);

    // when & then
    assertThatThrownBy(() -> tokenService.renew(refreshToken.token()))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @DisplayName("로그아웃을 할 수 있다.")
  @Test
  void logout() {
    Member member = memberRepository.save(new Member(
        null, new Email("test@example.com"), Password.of("password!", passwordEncoder), null));
    TokenPair tokenPair =
        tokenService.login(new LoginMember(member.getEmail().address(), "password!"));

    tokenService.logout(
        new LogoutMember(tokenPair.accessToken(), tokenPair.refreshToken().token()));

    String accessTokenJti = tokenProvider.extractJti(tokenPair.accessToken());
    String refreshTokenJti = tokenProvider.extractJti(tokenPair.refreshToken().token());

    assertThat(blacklistTokenRepository.exists(accessTokenJti)).isTrue();
    assertThat(blacklistTokenRepository.exists(refreshTokenJti)).isTrue();
  }

  @DisplayName("잘못된 형식의 액세스 토큰으로 로그아웃 시 예외가 발생한다.")
  @Test
  void logout_withInvalidAccessToken_throwsInvalidTokenException() {
    // given
    Member member = memberRepository.save(new Member(
        null, new Email("test@example.com"), Password.of("password!", passwordEncoder), null));
    TokenPair tokenPair =
        tokenService.login(new LoginMember(member.getEmail().address(), "password!"));

    // when & then
    assertThatThrownBy(() -> tokenService.logout(
            new LogoutMember("invalid-token", tokenPair.refreshToken().token())))
        .isInstanceOf(InvalidTokenException.class);
  }

  @DisplayName("잘못된 형식의 리프레시 토큰으로 로그아웃 시 예외가 발생한다.")
  @Test
  void logout_withInvalidRefreshToken_throwsInvalidTokenException() {
    // given
    Member member = memberRepository.save(new Member(
        null, new Email("test@example.com"), Password.of("password!", passwordEncoder), null));
    TokenPair tokenPair =
        tokenService.login(new LoginMember(member.getEmail().address(), "password!"));

    // when & then
    assertThatThrownBy(
            () -> tokenService.logout(new LogoutMember(tokenPair.accessToken(), "invalid-token")))
        .isInstanceOf(InvalidTokenException.class);
  }
}
