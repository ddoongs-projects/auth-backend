package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.NotFoundException;

public class MemberNotFoundException extends NotFoundException {

  public MemberNotFoundException() {
    super(CoreErrorCode.MEMBER_NOT_FOUND);
  }
}
