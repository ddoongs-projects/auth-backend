package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.member.RegisterMember;
import java.util.UUID;

public record MemberRegisterRequest(String email, String password, UUID verificationId) {

  public RegisterMember toRegisterMember() {
    return new RegisterMember(email, password);
  }
}
