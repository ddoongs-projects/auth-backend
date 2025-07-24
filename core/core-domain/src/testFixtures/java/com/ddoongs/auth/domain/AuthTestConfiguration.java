package com.ddoongs.auth.domain;

import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.support.FakeClock;
import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.domain.token.BlacklistTokenRepository;
import com.ddoongs.auth.domain.token.RefreshTokenRepository;
import com.ddoongs.auth.domain.token.TokenExchangeRepository;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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

  @Bean
  public BlacklistTokenRepository blacklistTokenRepository() {
    return TestFixture.blacklistTokenRepository();
  }

  @Bean
  public TokenExchangeRepository tokenExchangeRepository() {
    return TestFixture.tokenExchangeRepository();
  }

  @Primary
  @Bean
  public Clock fakeClock() {
    return new FakeClock(Instant.now(), ZoneId.systemDefault());
  }
}
