package com.ddoongs.auth.domain.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record RefreshToken(String jti, String subject, Instant expiresAt, String token) {

  public Duration remainingTtl(Clock clock) {
    Duration diff = Duration.between(Instant.now(clock), expiresAt);
    return diff.isNegative() ? Duration.ZERO : diff;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RefreshToken other)) {
      return false;
    }
    return Objects.equals(jti, other.jti);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jti);
  }
}
