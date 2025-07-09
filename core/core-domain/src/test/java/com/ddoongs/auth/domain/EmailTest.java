package com.ddoongs.auth.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

  @Test
  void email() {
    String address = "duk9741@gmail.com";
    assertThatCode(() -> new Email(address)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(strings = {"123123123", "a@.com", "a@123"})
  void invalidEmail(String invalidEmail) {
    assertThatThrownBy(() -> new Email(invalidEmail)).isInstanceOf(IllegalArgumentException.class);
  }
}
