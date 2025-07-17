package com.ddoongs.auth.domain.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.member.InvalidTokenException;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberNotFoundException;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.Password;
import com.ddoongs.auth.domain.shared.Email;
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
  private TokenProvider tokenProvider;

  @Autowired
  private MemberRepository memberRepository;

  @Test
  @DisplayName("유효한 리프레시 토큰으로 재발급 시 새로운 토큰 쌍을 발급한다")
  void reissue_withValidToken_issuesNewTokenPair() {
    // given
    Member member = memberRepository.save(
        new Member(null, new Email("test@example.com"), new Password("password"), null));
    RefreshToken refreshToken = tokenProvider.createRefreshToken(member);
    refreshTokenRepository.save(refreshToken);

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
    RefreshToken refreshToken = tokenProvider.createRefreshToken(member);
    refreshTokenRepository.save(refreshToken); // 사용자는 저장하지 않고 토큰만 저장

    // when & then
    assertThatThrownBy(() -> tokenService.reissue(refreshToken.token()))
        .isInstanceOf(MemberNotFoundException.class);
  }
}
