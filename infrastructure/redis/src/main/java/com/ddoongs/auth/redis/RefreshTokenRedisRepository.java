package com.ddoongs.auth.redis;

import com.ddoongs.auth.domain.token.RefreshToken;
import com.ddoongs.auth.domain.token.RefreshTokenRepository;
import java.time.Clock;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRedisRepository implements RefreshTokenRepository {

  private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken:";

  private final RedisTemplate<String, RefreshToken> redisTemplate;
  private final Clock clock;

  @Override
  public RefreshToken save(RefreshToken token) {
    String key = REFRESH_TOKEN_KEY_PREFIX + token.jti();
    redisTemplate.opsForValue().set(key, token, token.remainingTtl(clock));
    return token;
  }

  @Override
  public Optional<RefreshToken> find(String jti) {
    String key = REFRESH_TOKEN_KEY_PREFIX + jti;
    RefreshToken token = redisTemplate.opsForValue().get(key);
    return Optional.ofNullable(token);
  }

  @Override
  public void delete(String jti) {
    String key = "refresh:" + jti;
    redisTemplate.delete(key);
  }
}
