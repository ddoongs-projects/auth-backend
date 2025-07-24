package com.ddoongs.auth.domain.token;

import java.util.Optional;
import java.util.UUID;

public interface TokenExchangeRepository {

  TokenExchange save(TokenExchange tokenExchange);

  Optional<TokenExchange> find(UUID authCode);

  void delete(UUID authCode);
}
