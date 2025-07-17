package com.ddoongs.auth.domain;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;

public class ServiceTestSupport {

  private ServiceTestSupport() {}

  public static Verification prepareRegister(
      VerificationService verificationService, String email) {
    CreateVerification createVerification =
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER);
    Verification verification = verificationService.issue(createVerification);

    verificationService.verify(verification.getId(), new VerificationCode("123456"));
    return verification;
  }
}
