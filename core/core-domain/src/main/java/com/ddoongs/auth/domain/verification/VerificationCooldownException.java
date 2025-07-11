package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.BusinessException;
import java.time.Duration;
import lombok.Getter;

@Getter
public class VerificationCooldownException extends BusinessException {

  private final Duration remainCooldown;

  public VerificationCooldownException(final Duration remainCooldown) {
    super("요청 간격이 너무 짧습니다. " + remainCooldown.toSeconds() + "초 후에 시도해 주세요.");
    this.remainCooldown = remainCooldown;
  }
}
