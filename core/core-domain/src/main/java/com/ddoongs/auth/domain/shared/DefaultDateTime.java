package com.ddoongs.auth.domain.shared;

import java.time.LocalDateTime;

public record DefaultDateTime(LocalDateTime createdAt, LocalDateTime updatedAt) {

  public static DefaultDateTime now() {
    LocalDateTime now = LocalDateTime.now();
    return new DefaultDateTime(now, now);
  }
}
