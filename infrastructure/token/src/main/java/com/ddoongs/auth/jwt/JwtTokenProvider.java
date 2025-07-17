package com.ddoongs.auth.jwt;

import com.ddoongs.auth.domain.member.InvalidTokenException;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.token.RefreshToken;
import com.ddoongs.auth.domain.token.TokenExpiredException;
import com.ddoongs.auth.domain.token.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {

  private final SecretKey key;

  public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  private static String extractSubject(Member member) {
    return member.getEmail().address();
  }

  @Override
  public void validate(String token) {
    getClaims(token);
  }

  @Override
  public String extractJti(String token) {
    return getClaims(token).getId();
  }

  @Override
  public String extractSubject(String token) {
    return getClaims(token).getSubject();
  }

  @Override
  public Duration getRemainingAccessTtl(String token) {
    Instant exp = getClaims(token).getExpiration().toInstant();
    return Duration.between(Instant.now(), exp);
  }

  private Claims getClaims(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException();
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException();
    }
  }

  public String createAccessToken(Member member, Duration accessExpires) {
    String subject = extractSubject(member);

    Instant now = Instant.now();
    Instant exp = now.plus(accessExpires);

    String jti = UUID.randomUUID().toString();
    return createToken(jti, subject, now, exp);
  }

  private String createToken(String jti, String email, Instant now, Instant exp) {
    return Jwts.builder()
        .id(jti)
        .subject(email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();
  }

  public RefreshToken createRefreshToken(Member member, Duration refreshExpires) {
    String subject = extractSubject(member);

    String jti = UUID.randomUUID().toString();
    Instant now = Instant.now();
    Instant exp = now.plus(refreshExpires);

    String token = createToken(jti, subject, now, exp);

    return new RefreshToken(jti, subject, exp, token);
  }
}
