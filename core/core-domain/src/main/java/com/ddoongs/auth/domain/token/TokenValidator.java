package com.ddoongs.auth.domain.token;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenValidator {

  private final Duration renewThreshold;
  private final Clock clock;

  public TokenValidator(@Value("${jwt.renew-threshold}") Duration renewThreshold, Clock clock) {
    this.renewThreshold = renewThreshold;
    this.clock = clock;
  }

  public void validateRenewable(RefreshToken refreshToken) {
    if (!refreshToken.canBeRenewed(renewThreshold, clock)) {
      throw new TokenRenewalConditionNotMetException();
    }
  }
}
