package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.UnauthorizedException;

public class TokenExpiredException extends UnauthorizedException {

  public TokenExpiredException() {
    super(CoreErrorCode.EXPIRED_TOKEN);
  }
}
