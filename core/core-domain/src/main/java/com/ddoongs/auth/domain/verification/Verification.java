package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Verification {

  private final UUID id;
  private VerificationCode code;
  private Email email;
  private VerificationPurpose purpose;
  private VerificationStatus status;
  private DefaultDateTime defaultDateTime;

  private Verification() {
    this.id = UUID.randomUUID();
  }

  public static Verification create(
      CreateVerification createVerification, VerificationCodeGenerator verificationCodeGenerator) {
    Verification verification = new Verification();

    verification.code = verificationCodeGenerator.generate();
    verification.email = createVerification.email();
    verification.purpose = createVerification.purpose();

    verification.status = VerificationStatus.PENDING;

    verification.defaultDateTime = DefaultDateTime.now();

    return verification;
  }

  public void verify() {
    if (this.status == VerificationStatus.VERIFIED) {
      throw new VerificationAlreadyCompletedException();
    }

    this.status = VerificationStatus.VERIFIED;
  }
}
