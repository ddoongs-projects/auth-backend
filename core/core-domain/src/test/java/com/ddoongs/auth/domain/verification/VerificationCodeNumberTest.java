package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VerificationCodeNumberTest {

  @Test
  void constructor() {
    assertThatCode(() -> new VerificationNumber("123456")).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "1234567", "abcdef", "12345a"})
  void invalidValue(String invalidValue) {
    assertThatThrownBy(() -> new VerificationNumber(invalidValue))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
