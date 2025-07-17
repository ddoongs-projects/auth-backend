package com.ddoongs.auth.redis;

import com.ddoongs.auth.domain.token.BlacklistTokenRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BlacklistTokenRedisRepository implements BlacklistTokenRepository {

  private static final String BLACKLIST_TOKEN_KEY_PREFIX = "blacklistToken:";

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void save(String jti, Duration ttl) {
    String key = BLACKLIST_TOKEN_KEY_PREFIX + jti;
    stringRedisTemplate.opsForValue().set(key, "", ttl);
  }

  @Override
  public boolean exists(String jti) {
    String key = BLACKLIST_TOKEN_KEY_PREFIX + jti;
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
  }

  @Override
  public void delete(String jti) {
    String key = BLACKLIST_TOKEN_KEY_PREFIX + jti;
    stringRedisTemplate.delete(key);
  }
}
