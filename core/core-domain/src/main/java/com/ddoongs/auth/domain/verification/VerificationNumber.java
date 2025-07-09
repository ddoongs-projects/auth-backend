package com.ddoongs.auth.domain.verification;

import java.util.regex.Pattern;
import org.springframework.util.Assert;

public record VerificationNumber(String value) {

  private static final Pattern PATTERN = Pattern.compile("^\\d{6}$");

  public VerificationNumber {
    Assert.isTrue(PATTERN.matcher(value).matches(), "인증코드는 6자리 숫자여야 합니다.");
  }
}
