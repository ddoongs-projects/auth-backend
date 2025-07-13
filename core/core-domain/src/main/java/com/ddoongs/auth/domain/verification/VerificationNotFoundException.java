package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.NotFoundException;

public class VerificationNotFoundException extends NotFoundException {

  public VerificationNotFoundException() {
    super(CoreErrorCode.VERIFICATION_NOT_FOUND);
  }
}
