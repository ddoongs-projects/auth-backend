package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.Email;

public class VerificationFixture {

  private VerificationFixture() {}

  public static CreateVerification createVerification(VerificationPurpose purpose) {
    return new CreateVerification(new Email("duk9741@gmail.com"), purpose);
  }
}
