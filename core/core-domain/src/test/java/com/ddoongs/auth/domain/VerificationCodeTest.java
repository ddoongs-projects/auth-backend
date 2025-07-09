package com.ddoongs.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VerificationCodeTest {

  @Test
  void create() {
    String code = "123456";
    Email email = new Email("test@email.com");
    VerificationPurpose purpose = VerificationPurpose.REGISTER;
    VerificationCode verificationCode = VerificationCode.create(code, email, purpose);

    assertThat(verificationCode.getId()).isNotNull();
    assertThat(verificationCode.getCode().value()).isEqualTo(code);
    assertThat(verificationCode.getEmail()).isEqualTo(email);
    assertThat(verificationCode.getPurpose()).isEqualTo(purpose);
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "1234567", "abcdef", "12345a"})
  void invalidCode(String invalidCode) {
    Email email = new Email("test@email.com");
    VerificationPurpose purpose = VerificationPurpose.RESET_PASSWORD;

    assertThatThrownBy(() -> VerificationCode.create(invalidCode, email, purpose))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
