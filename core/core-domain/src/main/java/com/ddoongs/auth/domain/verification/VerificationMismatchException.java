package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;

public class VerificationMismatchException extends ValidationException {

  public VerificationMismatchException() {
    super(CoreErrorCode.VERIFICATION_MISMATCH);
  }
}
