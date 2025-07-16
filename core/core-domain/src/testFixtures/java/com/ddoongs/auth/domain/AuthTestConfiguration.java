package com.ddoongs.auth.domain;

import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.member.RefreshTokenRepository;
import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AuthTestConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return TestFixture.passwordEncoder();
  }

  @Bean
  public VerificationCodeGenerator verificationCodeGenerator() {
    return TestFixture.verificationCodeGenerator();
  }

  @Bean
  public RefreshTokenRepository refreshTokenRepository() {
    return TestFixture.refreshTokenRepository();
  }
}
