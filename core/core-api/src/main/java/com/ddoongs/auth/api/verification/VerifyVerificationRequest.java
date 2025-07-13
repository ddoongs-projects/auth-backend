package com.ddoongs.auth.api.verification;

import com.ddoongs.auth.domain.verification.VerificationCode;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VerifyVerificationRequest(@NotNull UUID verificationId, @NotNull String code) {

  public VerificationCode toCode() {
    return new VerificationCode(code);
  }
}
