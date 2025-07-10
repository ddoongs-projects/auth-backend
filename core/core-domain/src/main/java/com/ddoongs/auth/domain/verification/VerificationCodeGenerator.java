package com.ddoongs.auth.domain.verification;

/**
 * 6자리 숫자 코드를 생성한다.
 */
public interface VerificationCodeGenerator {

  VerificationCode generate();
}
