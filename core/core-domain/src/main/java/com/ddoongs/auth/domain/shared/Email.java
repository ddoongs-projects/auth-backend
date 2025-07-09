package com.ddoongs.auth.domain.shared;

import java.util.regex.Pattern;
import org.springframework.util.Assert;

public record Email(String address) {

  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  public Email {
    Assert.isTrue(EMAIL_PATTERN.matcher(address).matches(), "유효하지 않은 이메일 형식입니다.: " + address);
  }
}
