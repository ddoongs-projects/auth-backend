package com.ddoongs.auth.domain.member;

import java.util.UUID;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

@EqualsAndHashCode
@Getter
public class Password {

  private static final String PASSWORD_REGEX =
      "^[a-zA-Z0-9!@#$%^&*()_+-=\\[\\]{}|;':\",./<>?~]{8,32}$";

  private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

  private final String passwordHash;

  public Password(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public static Password of(String password, PasswordEncoder passwordEncoder) {
    Assert.isTrue(PASSWORD_PATTERN.matcher(password).matches(), "비밀번호 형식이 일치하지 않습니다.");
    String passwordHash = passwordEncoder.encode(password);
    return new Password(passwordHash);
  }

  public static Password ofRandom(PasswordEncoder passwordEncoder) {
    String password = UUID.randomUUID().toString();
    return new Password(passwordEncoder.encode(password));
  }

  public boolean matches(String password, PasswordEncoder passwordEncoder) {
    return passwordEncoder.matches(password, this.passwordHash);
  }
}
