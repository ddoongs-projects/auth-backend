package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.token.RefreshTokenRepository;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;

public class TestFixture {

  private TestFixture() {}

  public static PasswordEncoder passwordEncoder() {
    return new FakePasswordEncoder();
  }

  public static VerificationCodeGenerator verificationCodeGenerator() {
    return new FakeVerificationCodeGenerator();
  }

  public static VerificationCodeGenerator verificationCodeGenerator(String code) {
    FakeVerificationCodeGenerator fakeVerificationCodeGenerator =
        new FakeVerificationCodeGenerator();
    fakeVerificationCodeGenerator.setFixedCode(code);
    return fakeVerificationCodeGenerator;
  }

  public static RefreshTokenRepository refreshTokenRepository() {
    return new FakeRefreshTokenRepository();
  }
}
