package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.member.Member;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuer {

  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  private final Duration accessTokenValidity;
  private final Duration refreshTokenValidity;

  public TokenIssuer(
      TokenProvider tokenProvider,
      RefreshTokenRepository refreshTokenRepository,
      @Value("${jwt.access-token-validity}") Duration accessTokenValidity,
      @Value("${jwt.refresh-token-validity}") Duration refreshTokenValidity) {
    this.tokenProvider = tokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
    this.accessTokenValidity = accessTokenValidity;
    this.refreshTokenValidity = refreshTokenValidity;
  }

  public RefreshToken issueRefreshToken(Member member) {
    RefreshToken refreshToken = tokenProvider.createRefreshToken(member, refreshTokenValidity);
    refreshTokenRepository.save(refreshToken);
    return refreshToken;
  }

  public String issueAccessToken(Member member) {
    return tokenProvider.createAccessToken(member, accessTokenValidity);
  }
}
