package com.ddoongs.auth.api.auth;

import com.ddoongs.auth.domain.token.LogoutMember;

public record MemberLogoutRequest(String accessToken, String refreshToken) {

  public LogoutMember toLogoutMember() {
    return new LogoutMember(accessToken, refreshToken);
  }
}
