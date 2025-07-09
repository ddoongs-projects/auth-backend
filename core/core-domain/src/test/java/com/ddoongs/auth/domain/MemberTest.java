package com.ddoongs.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberTest {

  private PasswordEncoder passwordEncoder;

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

  @Test
  void registerFail() {

    assertThatThrownBy(
            () -> Member.register(new RegisterMember(null, "123asd!@#"), passwordEncoder))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(
            () -> Member.register(new RegisterMember("duk9741@gmail.com", null), passwordEncoder))
        .isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> Member.register(new RegisterMember(null, null), passwordEncoder))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void registerFailInvalidValue() {

    assertThatThrownBy(
            () -> Member.register(new RegisterMember("duk9741", "123asd!@#"), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () -> Member.register(new RegisterMember("@gmail.com", "123asd!@#"), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () -> Member.register(new RegisterMember("duk9741@gmail.com", "123"), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(
            () -> Member.register(new RegisterMember("gmail.com", "123"), passwordEncoder))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
