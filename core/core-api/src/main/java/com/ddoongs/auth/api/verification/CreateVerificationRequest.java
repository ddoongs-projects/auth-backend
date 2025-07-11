package com.ddoongs.auth.api.verification;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import jakarta.validation.constraints.NotNull;

public record CreateVerificationRequest(
    @jakarta.validation.constraints.Email String email, @NotNull VerificationPurpose purpose) {

  public CreateVerification toCommand() {
    return new CreateVerification(new Email(email), purpose);
  }
}
