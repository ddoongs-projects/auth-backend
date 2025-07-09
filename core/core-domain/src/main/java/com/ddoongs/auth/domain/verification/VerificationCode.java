package com.ddoongs.auth.domain.verification;

import java.util.regex.Pattern;
import org.springframework.util.Assert;

public record VerificationCode(String code) {

  private static final Pattern PATTERN = Pattern.compile("^\\d{6}$");

  public VerificationCode {
    Assert.isTrue(PATTERN.matcher(code).matches(), "인증코드는 6자리 숫자여야 합니다.");
  }
}
