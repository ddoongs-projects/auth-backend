package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.UnauthorizedException;

public class InvalidTokenException extends UnauthorizedException {

  public InvalidTokenException() {
    super(CoreErrorCode.INVALID_TOKEN);
  }
}
