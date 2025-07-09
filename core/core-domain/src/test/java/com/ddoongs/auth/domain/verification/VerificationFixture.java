package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import java.lang.reflect.Field;

public class VerificationFixture {

  private VerificationFixture() {}

  public static CreateVerification createVerification(VerificationPurpose purpose) {
    return new CreateVerification(new Email("duk9741@gmail.com"), purpose);
  }

  public static CreateVerification createVerification() {
    return createVerification(VerificationPurpose.REGISTER);
  }

  public static Verification verification(
      CreateVerification createVerification, VerificationCodeGenerator verificationCodeGenerator) {
    return Verification.create(createVerification(), verificationCodeGenerator);
  }

  public static Verification verification(VerificationCodeGenerator verificationCodeGenerator) {
    return Verification.create(createVerification(), verificationCodeGenerator);
  }

  public static Verification withDefaultDateTime(
      VerificationCodeGenerator verificationCodeGenerator, DefaultDateTime defaultDateTime) {
    try {
      Verification verification = verification(verificationCodeGenerator);

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
    try {
      Verification verification = verification(createVerification, verificationCodeGenerator);

      Field defaultDateTimeField = verification.getClass().getDeclaredField("defaultDateTime");
      defaultDateTimeField.setAccessible(true);
      defaultDateTimeField.set(verification, defaultDateTime);

      return verification;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create Verification instance for testing", e);
    }
  }
}
