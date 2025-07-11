package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
