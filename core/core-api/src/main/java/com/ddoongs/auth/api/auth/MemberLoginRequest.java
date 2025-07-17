package com.ddoongs.auth.api.auth;

import com.ddoongs.auth.domain.token.LoginMember;

public record MemberLoginRequest(String email, String password) {

  public LoginMember toLoginMember() {
    return new LoginMember(email, password);
  }
}
