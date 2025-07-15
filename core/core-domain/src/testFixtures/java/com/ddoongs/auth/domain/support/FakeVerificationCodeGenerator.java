package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;

public class FakeVerificationCodeGenerator implements VerificationCodeGenerator {

  private String fixedCode = "123456";

  @Override
  public VerificationCode generate() {
    return new VerificationCode(fixedCode);
  }

  public void setFixedCode(String fixedCode) {
    this.fixedCode = fixedCode;
  }
}
