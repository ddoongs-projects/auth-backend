package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.member.PasswordEncoder;
import java.util.Objects;

public class FakePasswordEncoder implements PasswordEncoder {

  @Override
  public String encode(String password) {
    return String.valueOf(Objects.hash(password));
  }

  @Override
  public boolean matches(String password, String passwordHash) {
    return encode(password).equals(passwordHash);
  }
}
