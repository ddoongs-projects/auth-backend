package com.ddoongs.auth.domain.token;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenValidator {

  private final Duration renewThreshold;

  public TokenValidator(@Value("${jwt.renew-threshold}") Duration renewThreshold) {
    this.renewThreshold = renewThreshold;
  }

  public void validateRenewable(RefreshToken refreshToken) {
    if (!refreshToken.canBeRenewed(renewThreshold)) {
      throw new TokenRenewalConditionNotMetException();
    }
  }
}
