package com.ddoongs.auth.domain.token;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  @Test
  void equalsAndHashCode() {
    String jti = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479").toString();

    RefreshToken refreshToken1 = new RefreshToken(
        jti, "test@test.com", Instant.now().plusSeconds(10), "sample.refresh.token");

    RefreshToken refreshToken2 = new RefreshToken(
        jti, "test@test.com", Instant.now().plusSeconds(10), "sample.refresh.token");

    assertThat(refreshToken1.equals(refreshToken2)).isTrue();
    assertThat(refreshToken1.hashCode()).isEqualTo(refreshToken2.hashCode());
  }
}
