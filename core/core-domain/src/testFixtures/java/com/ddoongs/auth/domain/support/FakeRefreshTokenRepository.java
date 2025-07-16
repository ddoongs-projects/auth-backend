package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.member.RefreshToken;
import com.ddoongs.auth.domain.member.RefreshTokenRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeRefreshTokenRepository implements RefreshTokenRepository {

  private final Map<String, RefreshToken> store = new HashMap<>();

  @Override
  public RefreshToken save(RefreshToken token) {
    store.put(token.jti(), token);
    return token;
  }

  @Override
  public Optional<RefreshToken> find(String jti) {
    return Optional.ofNullable(store.get(jti));
  }

  @Override
  public void delete(String jti) {
    store.remove(jti);
  }
}
