package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.UnauthorizedException;

public class InvalidAuthCodeException extends UnauthorizedException {

  public InvalidAuthCodeException() {
    super(CoreErrorCode.INVALID_AUTH_CODE);
  }
}
