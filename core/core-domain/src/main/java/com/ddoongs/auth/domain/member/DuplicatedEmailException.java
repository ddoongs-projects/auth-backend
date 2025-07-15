package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.ConflictException;
import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.Email;

public class DuplicatedEmailException extends ConflictException {

  public DuplicatedEmailException(Email email) {
    super(CoreErrorCode.DUPLICATED_EMAIL, email.address());
  }
}
