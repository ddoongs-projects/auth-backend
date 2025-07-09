package com.ddoongs.auth.domain;

import java.util.Objects;

public class TestFixture {

  private TestFixture() {}

  public static PasswordEncoder passwordEncoder() {
    return new PasswordEncoder() {
      @Override
      public String encode(String password) {
        return String.valueOf(Objects.hash(password));
      }

      @Override
      public boolean matches(String password, String passwordHash) {
        return encode(password).equals(passwordHash);
      }
    };
  }
}
