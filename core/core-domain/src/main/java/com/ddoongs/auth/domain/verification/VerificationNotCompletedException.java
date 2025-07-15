package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;

public class VerificationNotCompletedException extends ValidationException {

  public VerificationNotCompletedException() {
    super(CoreErrorCode.VERIFICATION_NOT_COMPLETED);
  }
}
