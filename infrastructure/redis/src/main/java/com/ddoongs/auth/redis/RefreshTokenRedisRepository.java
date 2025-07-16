package com.ddoongs.auth.redis;

import com.ddoongs.auth.domain.member.RefreshToken;
import com.ddoongs.auth.domain.member.RefreshTokenRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRedisRepository implements RefreshTokenRepository {

  private final RedisTemplate<String, RefreshToken> redisTemplate;

  @Override
  public RefreshToken save(RefreshToken token) {
    Duration ttl = token.remainingTtl();
    redisTemplate.opsForValue().set(token.jti(), token, ttl);
    return token;
  }

  @Override
  public Optional<RefreshToken> find(String jti) {
    RefreshToken token = redisTemplate.opsForValue().get(jti);
    return Optional.ofNullable(token);
  }

  @Override
  public void delete(String jti) {
    redisTemplate.delete(jti);
  }
}
