package com.ddoongs.auth.domain.member;

import java.time.Duration;

public interface TokenProvider {

  void validate(String token);

  String extractJti(String token);

  String getSubject(String token);

  Duration getRemainingAccessTtl(String token);

  String createAccessToken(Member member);

  RefreshToken createRefreshToken(Member member);
}
