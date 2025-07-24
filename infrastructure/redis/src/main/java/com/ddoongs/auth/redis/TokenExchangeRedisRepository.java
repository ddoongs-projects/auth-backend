package com.ddoongs.auth.redis;

import com.ddoongs.auth.domain.token.TokenExchange;
import com.ddoongs.auth.domain.token.TokenExchangeRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TokenExchangeRedisRepository implements TokenExchangeRepository {

  private static final String TOKEN_EXCHANGE_KEY_PREFIX = "tokenExchange:";
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

  private final RedisTemplate<String, TokenExchange> redisTemplate;

  @Override
  public TokenExchange save(TokenExchange tokenExchange) {
    String key = TOKEN_EXCHANGE_KEY_PREFIX + tokenExchange.authCode().toString();
    redisTemplate.opsForValue().set(key, tokenExchange, DEFAULT_TTL);
    return tokenExchange;
  }

  @Override
  public Optional<TokenExchange> find(UUID authCode) {
    String key = TOKEN_EXCHANGE_KEY_PREFIX + authCode.toString();
    TokenExchange tokenExchange = redisTemplate.opsForValue().get(key);
    return Optional.ofNullable(tokenExchange);
  }

  @Override
  public void delete(UUID authCode) {
    String key = "refresh:" + authCode.toString();
    redisTemplate.delete(key);
  }
}
