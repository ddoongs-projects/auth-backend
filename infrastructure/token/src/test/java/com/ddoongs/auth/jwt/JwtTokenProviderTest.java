package com.ddoongs.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.ddoongs.auth.domain.member.InvalidTokenException;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.RefreshToken;
import com.ddoongs.auth.domain.member.TokenExpiredException;
import com.ddoongs.auth.domain.support.FakePasswordEncoder;
import com.ddoongs.auth.domain.support.MemberFixture;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String TEST_SECRET =
      "dGVzdC1zZWNyZXQta2V5LXRlc3Qtc2VjcmV0LWtleS10ZXN0LXNlY3JldC1rZXk="; // "test-secret-key-test-secret-key-test-secret-key" in base64
  private static final long ACCESS_TOKEN_VALIDITY = 3600; // 1 hour
  private static final long REFRESH_TOKEN_VALIDITY = 86400; // 1 day

  private JwtTokenProvider jwtTokenProvider;
  private Member testMember;

  @BeforeEach
  void setUp() {
    jwtTokenProvider =
        new JwtTokenProvider(TEST_SECRET, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY);
    testMember = MemberFixture.member(new FakePasswordEncoder());
  }

  @Test
  @DisplayName("유효한 토큰은 검증에 성공한다")
  void validate_validToken_shouldNotThrowException() {
    // given
    String accessToken = jwtTokenProvider.createAccessToken(testMember);

    // when & then
    assertDoesNotThrow(() -> jwtTokenProvider.validate(accessToken));
  }

  @Test
  @DisplayName("만료된 토큰은 TokenExpiredException을 발생시킨다")
  void validate_expiredToken_shouldThrowTokenExpiredException() {
    // given
    JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(TEST_SECRET, 0, 0);
    String expiredToken = expiredTokenProvider.createAccessToken(testMember);

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
        ACCESS_TOKEN_VALIDITY,
        REFRESH_TOKEN_VALIDITY);
    String tokenWithWrongSignature = otherProvider.createAccessToken(testMember);

    // when & then
    assertThatThrownBy(() -> jwtTokenProvider.validate(tokenWithWrongSignature))
        .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  @DisplayName("토큰에서 Subject(email)를 추출한다")
  void getSubject_success() {
    // given
    String accessToken = jwtTokenProvider.createAccessToken(testMember);

    // when
    String subject = jwtTokenProvider.getSubject(accessToken);

    // then
    assertThat(subject).isEqualTo(testMember.getEmail().address());
  }

  @Test
  @DisplayName("Refresh Token에서 JTI를 추출한다")
  void extractJti_success() {
    // given
    RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(testMember);

    // when
    String jti = jwtTokenProvider.extractJti(refreshToken.token());

    // then
    assertThat(jti).isEqualTo(refreshToken.jti());
  }

  @Test
  @DisplayName("Access Token의 남은 유효시간(TTL)을 계산한다")
  void getRemainingAccessTtl_success() throws InterruptedException {
    // given
    JwtTokenProvider shortLivedTokenProvider =
        new JwtTokenProvider(TEST_SECRET, 2, REFRESH_TOKEN_VALIDITY);
    String accessToken = shortLivedTokenProvider.createAccessToken(testMember);

    // when
    Thread.sleep(1000); // 1초 대기
    Duration remainingTtl = shortLivedTokenProvider.getRemainingAccessTtl(accessToken);

    // then
    assertThat(remainingTtl.getSeconds()).isLessThanOrEqualTo(1);
  }

  @Nested
  @DisplayName("Access Token 생성")
  class CreateAccessToken {

    @Test
    @DisplayName("멤버 정보를 기반으로 Access Token을 생성한다")
    void createAccessToken_success() {
      // when
      String accessToken = jwtTokenProvider.createAccessToken(testMember);

      // then
      assertThat(accessToken).isNotNull();
      String subject = jwtTokenProvider.getSubject(accessToken);
      assertThat(subject).isEqualTo(testMember.getEmail().address());
    }
  }

  @Nested
  @DisplayName("Refresh Token 생성")
  class CreateRefreshToken {

    @Test
    @DisplayName("멤버 정보를 기반으로 Refresh Token을 생성한다")
    void createRefreshToken_success() {
      // when
      RefreshToken refreshToken = jwtTokenProvider.createRefreshToken(testMember);

      // then
      assertThat(refreshToken).isNotNull();
      assertThat(refreshToken.jti()).isNotNull();
      assertThat(refreshToken.token()).isNotNull();
      assertThat(refreshToken.subject()).isEqualTo(testMember.getEmail().address());

      String subject = jwtTokenProvider.getSubject(refreshToken.token());
      assertThat(subject).isEqualTo(testMember.getEmail().address());
      String jti = jwtTokenProvider.extractJti(refreshToken.token());
      assertThat(jti).isEqualTo(refreshToken.jti());
    }
  }
}
