package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.UnauthorizedException;

public class TokenRenewalConditionNotMetException extends UnauthorizedException {

  public TokenRenewalConditionNotMetException() {
    super(CoreErrorCode.TOKEN_RENEWAL_CONDITION_NOT_MET);
  }
}
