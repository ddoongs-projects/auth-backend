package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import java.lang.reflect.Field;

public class VerificationFixture {

  private static final String DEFAULT_EMAIL = "duk9741@gmail.com";

  private VerificationFixture() {}

  public static CreateVerification createVerification(VerificationPurpose purpose) {
    return new CreateVerification(new Email(DEFAULT_EMAIL), purpose);
  }

  public static CreateVerification createVerification() {
    return createVerification(VerificationPurpose.REGISTER);
  }

  public static Verification verification(
      CreateVerification createVerification, VerificationCodeGenerator verificationCodeGenerator) {
    return Verification.create(createVerification, verificationCodeGenerator);
  }

  public static Verification verification(VerificationCodeGenerator verificationCodeGenerator) {
    return Verification.create(createVerification(), verificationCodeGenerator);
  }

  public static Verification withDefaultDateTime(
      VerificationCodeGenerator verificationCodeGenerator, DefaultDateTime defaultDateTime) {
    Verification verification = verification(verificationCodeGenerator);
    return changeDefaultDateTime(verification, defaultDateTime);
  }

  private static Verification changeDefaultDateTime(
      Verification verification, DefaultDateTime defaultDateTime) {

    try {
      Field defaultDateTimeField = verification.getClass().getDeclaredField("defaultDateTime");
      defaultDateTimeField.setAccessible(true);
      defaultDateTimeField.set(verification, defaultDateTime);

      return verification;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create Verification instance for testing", e);
    }
  }

  public static Verification withDefaultDateTime(
      CreateVerification createVerification,
      VerificationCodeGenerator verificationCodeGenerator,
      DefaultDateTime defaultDateTime) {
    Verification verification = verification(createVerification, verificationCodeGenerator);
    return changeDefaultDateTime(verification, defaultDateTime);
  }
}
