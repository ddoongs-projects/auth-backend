package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;

public class TestFixture {

  public static final String FIXED_CODE = "123456";

  private TestFixture() {}

  public static PasswordEncoder passwordEncoder() {
    return new FakePasswordEncoder();
  }

  public static VerificationCodeGenerator verificationCodeGenerator(String code) {
    return () -> new VerificationCode(code);
  }

  public static VerificationCodeGenerator verificationCodeGenerator() {
    return verificationCodeGenerator(FIXED_CODE);
  }
}
