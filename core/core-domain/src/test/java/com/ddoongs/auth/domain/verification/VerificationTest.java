package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;

import com.ddoongs.auth.domain.TestFixture;
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
}
