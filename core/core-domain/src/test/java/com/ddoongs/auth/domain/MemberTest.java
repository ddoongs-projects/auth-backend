package com.ddoongs.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MemberTest {

  private PasswordEncoder passwordEncoder;

  static Stream<Arguments> provideNullForRegister() {
    return Stream.of(
        Arguments.of(new RegisterMember(null, "123asd!@#")),
        Arguments.of(new RegisterMember("duk9741@gmail.com", null)),
        Arguments.of(new RegisterMember(null, null)));
  }

  @BeforeEach
  void setup() {
    passwordEncoder = TestFixture.passwordEncoder();
  }

  @Test
  void register() {
    RegisterMember registerMember = MemberFixture.registerMember();
    Member member = Member.register(registerMember, passwordEncoder);

    assertThat(member.getEmail().address()).isEqualTo(registerMember.email());
    assertThat(member.getPassword().getPasswordHash()).isNotEqualTo(registerMember.password());
  }

  @ParameterizedTest
  @MethodSource("provideNullForRegister")
  void registerFail(RegisterMember registerMember) {
    assertThatThrownBy(() -> Member.register(registerMember, passwordEncoder))
        .isInstanceOf(NullPointerException.class);
  }

  @ParameterizedTest
  @CsvSource({
    "duk9741, 123asd!@#",
    "@gmail.com, 123asd!@#",
    "duk9741@gmail.com, 123",
    "gmail.com, 123"
  })
  void registerFailInvalidValue(String email, String password) {
    assertThatThrownBy(() -> Member.register(new RegisterMember(email, password), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
