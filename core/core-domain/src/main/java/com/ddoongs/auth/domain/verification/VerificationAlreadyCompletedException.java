package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;

public class VerificationAlreadyCompletedException extends ValidationException {

  public VerificationAlreadyCompletedException() {
    super(CoreErrorCode.VERIFICATION_ALREADY_COMPLETED);
  }
}
