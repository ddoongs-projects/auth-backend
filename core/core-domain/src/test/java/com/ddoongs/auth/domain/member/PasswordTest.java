package com.ddoongs.auth.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.domain.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

  @ParameterizedTest
  @ValueSource(
      strings = {
        "aaaaaaaa", // 8자리
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 32자리
        "11111111", // 8자리
        "11111111111111111111111111111111", // 32자리
        "********", // 8자리
        "********************************", // 32자리
        "1*a1*a1*a1*a1*a1*a1*a1*a1*a1*a1*" // 32자리
      })
  void validPassword(String validPassword) {
    assertThatCode(() -> Password.of(validPassword, passwordEncoder)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "aaaaaaa", // 7자리
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 33자리
        "김김김김김김김김", // 8자리
        "김김김김김김김김김김김김김김김김김김김김김김김김김김김김김김김김", // 32자리
        "1*a김1*a김1*a김1*a김1*a김1*a김1*a김1*a김" // 32자리
      })
  void invalidPassword(String invalidPassword) {
    assertThatThrownBy(() -> Password.of(invalidPassword, passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void matches() {
    Password password = Password.of("123qwe!@#", passwordEncoder);
    String samePassword = "123qwe!@#";

    assertThat(password.matches(samePassword, passwordEncoder)).isTrue();

    String differentPassword = "456rty%$^";

    assertThat(password.matches(differentPassword, passwordEncoder)).isFalse();
  }
}
