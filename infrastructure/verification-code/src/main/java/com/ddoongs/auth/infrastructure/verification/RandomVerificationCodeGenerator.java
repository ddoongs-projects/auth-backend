package com.ddoongs.auth.infrastructure.verification;

import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomVerificationCodeGenerator implements VerificationCodeGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();

  @Override
  public VerificationCode generate() {
    int number = RANDOM.nextInt(1_000_000);
    String code = String.format("%06d", number);
    return new VerificationCode(code);
  }
}
