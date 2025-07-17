package com.ddoongs.auth.domain.support;

import com.ddoongs.auth.domain.token.BlacklistTokenRepository;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class FakeBlacklistTokenRepository implements BlacklistTokenRepository {

  private final Set<String> store = new HashSet<>();

  @Override
  public void save(String jti, Duration ttl) {
    store.add(jti);
  }

  @Override
  public boolean exists(String jti) {
    return store.contains(jti);
  }

  @Override
  public void delete(String jti) {
    store.remove(jti);
  }
}
