package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;

public class AlreadyConsumedVerificationException extends ValidationException {

  public AlreadyConsumedVerificationException() {
    super(CoreErrorCode.ALREADY_CONSUMED_VERIFICATION);
  }
}
