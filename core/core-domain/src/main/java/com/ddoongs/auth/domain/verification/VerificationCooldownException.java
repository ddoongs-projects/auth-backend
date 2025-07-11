package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;
import java.time.Duration;
import lombok.Getter;

@Getter
public class VerificationCooldownException extends ValidationException {

  public VerificationCooldownException(final Duration remainCooldown) {
    super(CoreErrorCode.VERIFICATION_COOLDOWN, remainCooldown.toSeconds());
  }
}
