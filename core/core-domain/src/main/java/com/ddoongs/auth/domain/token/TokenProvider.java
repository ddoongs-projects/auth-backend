package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.member.Member;
import java.time.Duration;

public interface TokenProvider {

  void validate(String token);

  String extractJti(String token);

  String extractSubject(String token);

  Duration getRemainingAccessTtl(String token);

  String createAccessToken(Member member);

  RefreshToken createRefreshToken(Member member);
}
