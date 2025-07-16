package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.token.TokenPair;

public record TokenResponse(String accessToken, String refreshToken) {

  public static TokenResponse of(TokenPair tokenPair) {
    return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken().token());
  }
}
