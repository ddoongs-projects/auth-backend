package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;

public class InvalidVerificationCodeException extends ValidationException {

  public InvalidVerificationCodeException() {
    super(CoreErrorCode.INVALID_VERIFICATION_CODE);
  }
}
