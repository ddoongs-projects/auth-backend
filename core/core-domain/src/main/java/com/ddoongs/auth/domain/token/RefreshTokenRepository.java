package com.ddoongs.auth.domain.token;

import java.util.Optional;

public interface RefreshTokenRepository {

  RefreshToken save(RefreshToken token);

  Optional<RefreshToken> find(String jti);

  void delete(String jti);
}
