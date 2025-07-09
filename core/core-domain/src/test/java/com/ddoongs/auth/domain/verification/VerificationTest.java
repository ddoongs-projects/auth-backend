package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.domain.shared.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VerificationTest {

  @Test
  void create() {
    String code = "123456";
    Email email = new Email("test@email.com");
    VerificationPurpose purpose = VerificationPurpose.REGISTER;
    Verification verification = Verification.create(code, email, purpose);

    assertThat(verification.getId()).isNotNull();
    assertThat(verification.getCode().code()).isEqualTo(code);
    assertThat(verification.getEmail()).isEqualTo(email);
    assertThat(verification.getPurpose()).isEqualTo(purpose);
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "1234567", "abcdef", "12345a"})
  void invalidCode(String invalidCode) {
    Email email = new Email("test@email.com");
    VerificationPurpose purpose = VerificationPurpose.RESET_PASSWORD;

    assertThatThrownBy(() -> Verification.create(invalidCode, email, purpose))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
