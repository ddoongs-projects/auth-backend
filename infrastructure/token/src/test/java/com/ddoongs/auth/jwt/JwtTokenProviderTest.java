package com.ddoongs.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.support.FakePasswordEncoder;
import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.token.InvalidTokenException;
import com.ddoongs.auth.domain.token.RefreshToken;
import com.ddoongs.auth.domain.token.TokenExpiredException;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String TEST_SECRET =
      "dGVzdC1zZWNyZXQta2V5LXRlc3Qtc2VjcmV0LWtleS10ZXN0LXNlY3JldC1rZXk="; // "test-secret-key-test-secret-key-test-secret-key" in base64

  private JwtTokenProvider jwtTokenProvider;
  private Member testMember;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, Clock.systemDefaultZone());
    testMember = MemberFixture.member(new FakePasswordEncoder());
  }

  @Test
  @DisplayName("유효한 토큰은 검증에 성공한다")
  void validate_validToken_shouldNotThrowException() {
    // given
    String accessToken = jwtTokenProvider.createAccessToken(testMember, Duration.ofDays(1));

    // when & then
    assertDoesNotThrow(() -> jwtTokenProvider.validate(accessToken));
  }

  @Test
  @DisplayName("만료된 토큰은 TokenExpiredException을 발생시킨다")
  void validate_expiredToken_shouldThrowTokenExpiredException() {
    // given
    JwtTokenProvider expiredTokenProvider =
        new JwtTokenProvider(TEST_SECRET, Clock.systemDefaultZone());
    String expiredToken = expiredTokenProvider.createAccessToken(testMember, Duration.ZERO);

    // when & then
    assertThatThrownBy(() -> jwtTokenProvider.validate(expiredToken))
        .isInstanceOf(TokenExpiredException.class);
  }

  @Test
  @DisplayName("유효하지 않은 토큰은 InvalidTokenException을 발생시킨다")
  void validate_invalidToken_shouldThrowInvalidTokenException() {
    // given
    String invalidToken = "invalid.token.string";

    // when & then
    assertThatThrownBy(() -> jwtTokenProvider.validate(invalidToken))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("서명이 다른 토큰은 InvalidTokenException을 발생시킨다")
  void validate_wrongSignatureToken_shouldThrowInvalidTokenException() {
    // given
    JwtTokenProvider otherProvider = new JwtTokenProvider(
        "b3RoZXItc2VjcmV0LWtleS1vdGhlci1zZWNyZXQta2V5LW90aGVyLXNlY3JldC1rZXk=",
        Clock.systemDefaultZone());
    String tokenWithWrongSignature =
        otherProvider.createAccessToken(testMember, Duration.ofDays(1));

    // when & then
    assertThatThrownBy(() -> jwtTokenProvider.validate(tokenWithWrongSignature))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("토큰에서 Subject(email)를 추출한다")
  void extractSubject_success() {
    // given
    String accessToken = jwtTokenProvider.createAccessToken(testMember, Duration.ofDays(1));

    // when
    String subject = jwtTokenProvider.extractSubject(accessToken);

    // then
    assertThat(subject).isEqualTo(testMember.getEmail().address());
  }

  @Test
  @DisplayName("Refresh Token에서 JTI를 추출한다")
  void extractJti_success() {
    // given
    RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(testMember, Duration.ofDays(1));

    // when
    String jti = jwtTokenProvider.extractJti(refreshToken.token());

    // then
    assertThat(jti).isEqualTo(refreshToken.jti());
  }

  @Test
  @DisplayName("Access Token의 남은 유효시간(TTL)을 계산한다")
  void getRemainingAccessTtl_success() {
    // given
    JwtTokenProvider shortLivedTokenProvider =
        new JwtTokenProvider(TEST_SECRET, Clock.systemDefaultZone());
    String accessToken =
        shortLivedTokenProvider.createAccessToken(testMember, Duration.ofSeconds(1));

    // when
    Duration remainingTtl = shortLivedTokenProvider.getRemainingAccessTtl(accessToken);

    // then
    assertThat(remainingTtl.getSeconds()).isLessThanOrEqualTo(1);
  }

  @Test
  @DisplayName("멤버 정보를 기반으로 Access Token을 생성한다")
  void createAccessToken_success() {
    // when
    String accessToken = jwtTokenProvider.createAccessToken(testMember, Duration.ofDays(1));

    // then
    assertThat(accessToken).isNotNull();
    String subject = jwtTokenProvider.extractSubject(accessToken);
    assertThat(subject).isEqualTo(testMember.getEmail().address());
  }

  @Test
  @DisplayName("멤버 정보를 기반으로 Refresh Token을 생성한다")
  void createRefreshToken_success() {
    // when
    RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(testMember, Duration.ofDays(1));

    // then
    assertThat(refreshToken).isNotNull();
    assertThat(refreshToken.jti()).isNotNull();
    assertThat(refreshToken.token()).isNotNull();
    assertThat(refreshToken.subject()).isEqualTo(testMember.getEmail().address());

    String subject = jwtTokenProvider.extractSubject(refreshToken.token());
    assertThat(subject).isEqualTo(testMember.getEmail().address());
    String jti = jwtTokenProvider.extractJti(refreshToken.token());
    assertThat(jti).isEqualTo(refreshToken.jti());
  }
}
