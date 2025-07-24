package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.token.TokenExchange;
import com.ddoongs.auth.domain.token.TokenExchangeRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeTokenExchangeRepository implements TokenExchangeRepository {

  private final Map<String, TokenExchange> store = new HashMap<>();

  @Override
  public TokenExchange save(TokenExchange tokenExchange) {
    store.put(tokenExchange.authCode().toString(), tokenExchange);
    return tokenExchange;
  }

  @Override
  public Optional<TokenExchange> find(UUID authCode) {
    return Optional.ofNullable(store.get(authCode.toString()));
  }

  @Override
  public void delete(UUID authCode) {
    store.remove(authCode.toString());
  }
}
