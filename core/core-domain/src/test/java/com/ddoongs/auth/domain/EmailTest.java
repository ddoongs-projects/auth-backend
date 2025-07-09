package com.ddoongs.auth.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void email() {
    String address = "duk9741@gmail.com";
    assertThatCode(() -> new Email(address)).doesNotThrowAnyException();
  }

  @Test
  void invalidEmail() {
    assertThatThrownBy(() -> new Email("123123123")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email("a@.com")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email("a@123")).isInstanceOf(IllegalArgumentException.class);
  }
}
