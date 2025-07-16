package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.ValidationException;

public class PasswordMismatchException extends ValidationException {

  public PasswordMismatchException() {
    super(CoreErrorCode.PASSWORD_MISMATCH);
  }
}
