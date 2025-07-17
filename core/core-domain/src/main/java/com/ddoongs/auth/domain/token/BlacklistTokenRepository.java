package com.ddoongs.auth.domain.token;

import java.time.Duration;

public interface BlacklistTokenRepository {

  void save(String jti, Duration ttl);

  boolean exists(String jti);

  void delete(String jti);
}
