package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.domain.support.VerificationFixture;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VerificationTest {

  private VerificationCodeGenerator verificationCodeGenerator;

  @BeforeEach
  void setup() {
    verificationCodeGenerator = TestFixture.verificationCodeGenerator("123456");
  }

  @Test
  void create() {
    CreateVerification createVerification =
        VerificationFixture.createVerification(VerificationPurpose.REGISTER);
    Verification verification = Verification.create(createVerification, verificationCodeGenerator);

    assertThat(verification.getId()).isNotNull();
    assertThat(verification.getCode().code()).isEqualTo("123456");
    assertThat(verification.getEmail()).isEqualTo(createVerification.email());
    assertThat(verification.getPurpose()).isEqualTo(createVerification.purpose());
    assertThat(verification.getDefaultDateTime()).isNotNull();
  }

  @Test
  void verify() {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    assertThatCode(() -> verification.verify(new VerificationCode("123456")))
        .doesNotThrowAnyException();
  }

  @Test
  void verifyFailInvalidCode() {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    assertThatThrownBy(() -> verification.verify(new VerificationCode("000000")))
        .isInstanceOf(InvalidVerificationCodeException.class);
  }

  @Test
  void verifyFailAlreadyVerified() throws NoSuchFieldException, IllegalAccessException {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    Field statusField = Verification.class.getDeclaredField("status");
    statusField.setAccessible(true);
    statusField.set(verification, VerificationStatus.VERIFIED);

    assertThatThrownBy(() -> verification.verify(new VerificationCode("123456")))
        .isInstanceOf(VerificationAlreadyCompletedException.class);
  }

  @Test
  void verifyFailAlreadyConsumed() throws NoSuchFieldException, IllegalAccessException {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    Field statusField = Verification.class.getDeclaredField("status");
    statusField.setAccessible(true);
    statusField.set(verification, VerificationStatus.CONSUMED);

    assertThatThrownBy(() -> verification.verify(new VerificationCode("123456")))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void ensureValidFor() {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    verification.verify(new VerificationCode("123456"));

    assertThatCode(
            () -> verification.ensureValidFor(verification.getEmail(), verification.getPurpose()))
        .doesNotThrowAnyException();
  }

  @Test
  void ensureValidForFailDifferentEmail() {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    verification.verify(new VerificationCode("123456"));

    assertThatThrownBy(() ->
            verification.ensureValidFor(new Email("different@test.com"), verification.getPurpose()))
        .isInstanceOf(VerificationMismatchException.class);
  }

  @Test
  void ensureValidForFailDifferentPurpose() {
    Verification verification = VerificationFixture.verification(
        VerificationFixture.createVerification(VerificationPurpose.REGISTER),
        verificationCodeGenerator);

    verification.verify(new VerificationCode("123456"));

    assertThatThrownBy(() -> verification.ensureValidFor(
            verification.getEmail(), VerificationPurpose.RESET_PASSWORD))
        .isInstanceOf(VerificationMismatchException.class);
  }

  @Test
  void consume() {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);
    verification.verify(new VerificationCode("123456"));

    verification.consume();

    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.CONSUMED);
  }

  @Test
  void ensureValidForFailAlreadyConsumed() {
    Verification verification = VerificationFixture.verification(
        VerificationFixture.createVerification(VerificationPurpose.REGISTER),
        verificationCodeGenerator);

    verification.verify(new VerificationCode("123456"));
    verification.consume();

    assertThatThrownBy(() ->
            verification.ensureValidFor(verification.getEmail(), VerificationPurpose.REGISTER))
        .isInstanceOf(AlreadyConsumedVerificationException.class);
  }

  @Test
  void consumeFail() {
    Verification verification = VerificationFixture.verification(verificationCodeGenerator);

    assertThatThrownBy(verification::consume).isInstanceOf(IllegalStateException.class);

    verification.verify(new VerificationCode("123456"));
    verification.consume();

    assertThatThrownBy(verification::consume).isInstanceOf(IllegalStateException.class);
  }
}
