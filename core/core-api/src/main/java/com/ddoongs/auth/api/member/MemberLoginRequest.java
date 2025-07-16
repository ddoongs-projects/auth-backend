package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.member.LoginMember;

public record MemberLoginRequest(String email, String password) {

  public LoginMember toLoginMember() {
    return new LoginMember(email, password);
  }
}
