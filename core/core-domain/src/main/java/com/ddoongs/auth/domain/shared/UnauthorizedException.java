package com.ddoongs.auth.domain.shared;

public class UnauthorizedException extends BusinessException {

  public UnauthorizedException(CoreErrorCode code, Object... args) {
    super(code, args);
  }

  public UnauthorizedException(Object... args) {
    super(CoreErrorCode.UNAUTHORIZED, args);
  }
}
