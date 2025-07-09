package com.ddoongs.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordTest {

  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setup() {
    passwordEncoder = TestFixture.passwordEncoder();
  }

  @Test
  void password() {
    String rawPassword = "123asd!@#";

    Password password = Password.of(rawPassword, passwordEncoder);

    assertThat(password.getPasswordHash()).isNotEqualTo(rawPassword);
  }

  @Test
  void validPassword() {
    assertThatCode(() -> Password.of("a".repeat(8), passwordEncoder)).doesNotThrowAnyException();
    assertThatCode(() -> Password.of("a".repeat(32), passwordEncoder)).doesNotThrowAnyException();
    assertThatCode(() -> Password.of("1".repeat(8), passwordEncoder)).doesNotThrowAnyException();
    assertThatCode(() -> Password.of("1".repeat(32), passwordEncoder)).doesNotThrowAnyException();
    assertThatCode(() -> Password.of("*".repeat(8), passwordEncoder)).doesNotThrowAnyException();
    assertThatCode(() -> Password.of("*".repeat(32), passwordEncoder)).doesNotThrowAnyException();
    assertThatCode(() -> Password.of("1*a".repeat(32).substring(0, 32), passwordEncoder))
        .doesNotThrowAnyException();
  }

  @Test
  void invalidPassword() {
    assertThatThrownBy(() -> Password.of("a".repeat(7), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Password.of("a".repeat(33), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Password.of("김".repeat(8), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Password.of("김".repeat(32), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Password.of("1*a김".repeat(32).substring(0, 32), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
