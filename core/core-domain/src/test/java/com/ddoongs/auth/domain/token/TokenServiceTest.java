package com.ddoongs.auth.domain.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.ServiceTestSupport;
import com.ddoongs.auth.domain.member.AppendProviderDetail;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberNotFoundException;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.member.Password;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.member.PasswordMismatchException;
import com.ddoongs.auth.domain.member.Provider;
import com.ddoongs.auth.domain.member.RegisterMember;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
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

  @Autowired
  private TokenExchangeRepository tokenExchangeRepository;

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
    Member member = memberRepository.save(new Member(
        null, new Email("test@example.com"), new Password("password"), new ArrayList<>(), null));
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
    Member member = new Member(
        999L, new Email("ghost@example.com"), new Password("password"), new ArrayList<>(), null);
    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);

    // when & then
    assertThatThrownBy(() -> tokenService.reissue(refreshToken.token()))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @DisplayName("로그아웃을 할 수 있다.")
  @Test
  void logout() {
    Member member = memberRepository.save(new Member(
        null,
        new Email("test@example.com"),
        Password.of("password!", passwordEncoder),
        new ArrayList<>(),
        null));
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
        null,
        new Email("test@example.com"),
        Password.of("password!", passwordEncoder),
        new ArrayList<>(),
        null));
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
        null,
        new Email("test@example.com"),
        Password.of("password!", passwordEncoder),
        new ArrayList<>(),
        null));
    TokenPair tokenPair =
        tokenService.login(new LoginMember(member.getEmail().address(), "password!"));

    // when & then
    assertThatThrownBy(
            () -> tokenService.logout(new LogoutMember(tokenPair.accessToken(), "invalid-token")))
        .isInstanceOf(InvalidTokenException.class);
  }

  @DisplayName("OAuth2 회원의 토큰 교환을 준비할 수 있다")
  @Test
  void prepareTokenExchange() {
    // given
    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "oauth@test.com");
    Member member = memberService.registerOAuth2(providerDetail);

    PrepareTokenExchange prepareTokenExchange =
        new PrepareTokenExchange(Provider.GOOGLE, "google123");

    // when
    TokenExchange tokenExchange = tokenService.prepareTokenExchange(prepareTokenExchange);

    // then
    assertThat(tokenExchange.authCode()).isNotNull();
    assertThat(tokenExchange.tokenPair()).isNotNull();
    assertThat(tokenExchange.tokenPair().accessToken()).isNotNull();
    assertThat(tokenExchange.tokenPair().refreshToken()).isNotNull();

    // verify saved in repository
    assertThat(tokenExchangeRepository.find(tokenExchange.authCode())).isPresent();
  }

  @DisplayName("존재하지 않는 OAuth2 회원의 토큰 교환 준비 시 예외가 발생한다")
  @Test
  void prepareTokenExchange_memberNotFound() {
    // given
    PrepareTokenExchange prepareTokenExchange =
        new PrepareTokenExchange(Provider.GOOGLE, "nonexistent123");

    // when & then
    assertThatThrownBy(() -> tokenService.prepareTokenExchange(prepareTokenExchange))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @DisplayName("유효한 인증 코드로 토큰을 교환할 수 있다")
  @Test
  void exchangeToken() {
    // given
    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "oauth@test.com");
    Member member = memberService.registerOAuth2(providerDetail);

    PrepareTokenExchange prepareTokenExchange =
        new PrepareTokenExchange(Provider.GOOGLE, "google123");
    TokenExchange preparedExchange = tokenService.prepareTokenExchange(prepareTokenExchange);

    // when
    TokenPair exchangedTokenPair = tokenService.exchangeToken(preparedExchange.authCode());

    // then
    assertThat(exchangedTokenPair).isNotNull();
    assertThat(exchangedTokenPair.accessToken())
        .isEqualTo(preparedExchange.tokenPair().accessToken());
    assertThat(exchangedTokenPair.refreshToken())
        .isEqualTo(preparedExchange.tokenPair().refreshToken());
  }

  @DisplayName("존재하지 않는 인증 코드로 토큰 교환 시 예외가 발생한다")
  @Test
  void exchangeToken_invalidAuthCode() {
    // given
    UUID invalidAuthCode = UUID.randomUUID();

    // when & then
    assertThatThrownBy(() -> tokenService.exchangeToken(invalidAuthCode))
        .isInstanceOf(InvalidAuthCodeException.class);
  }

  @DisplayName("여러 OAuth2 제공자를 가진 회원의 토큰 교환을 준비할 수 있다")
  @Test
  void prepareTokenExchange_withMultipleProviders() {
    // given
    String email = "multi@test.com";
    Member member = memberRepository.save(new Member(
        null,
        new Email(email),
        Password.of("password123", passwordEncoder),
        new ArrayList<>(),
        null));

    AppendProviderDetail googleDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", email);
    AppendProviderDetail kakaoDetail = new AppendProviderDetail(Provider.KAKAO, "kakao456", email);

    memberService.registerOAuth2(googleDetail);
    memberService.registerOAuth2(kakaoDetail);

    // when
    PrepareTokenExchange googlePrepare = new PrepareTokenExchange(Provider.GOOGLE, "google123");
    PrepareTokenExchange kakaoPrepare = new PrepareTokenExchange(Provider.KAKAO, "kakao456");

    TokenExchange googleExchange = tokenService.prepareTokenExchange(googlePrepare);
    TokenExchange kakaoExchange = tokenService.prepareTokenExchange(kakaoPrepare);

    // then
    assertThat(googleExchange.authCode()).isNotEqualTo(kakaoExchange.authCode());
    assertThat(googleExchange.tokenPair()).isNotEqualTo(kakaoExchange.tokenPair());

    // both should be exchangeable
    TokenPair googleTokens = tokenService.exchangeToken(googleExchange.authCode());
    TokenPair kakaoTokens = tokenService.exchangeToken(kakaoExchange.authCode());

    assertThat(googleTokens).isNotNull();
    assertThat(kakaoTokens).isNotNull();
  }
}
