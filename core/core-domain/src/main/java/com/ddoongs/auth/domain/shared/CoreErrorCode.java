package com.ddoongs.auth.domain.shared;

import static com.ddoongs.auth.domain.shared.CoreErrorLevel.WARN;

import lombok.Getter;

@Getter
public enum CoreErrorCode {
  INVALID_REQUEST(WARN, "잘못된 요청입니다."),
  NOT_FOUND(WARN, "리소스를 찾을 수 없습니다."),
  CONFLICT(WARN, "이미 존재하는 리소스입니다."),
  VERIFICATION_COOLDOWN(WARN, "인증 발급 요청 간격이 너무 짧습니다. %d초 후에 시도해 주세요."),
  ;

  private final String defaultMessage;
  private final CoreErrorLevel level;

  CoreErrorCode(CoreErrorLevel level, String defaultMessage) {
    this.defaultMessage = defaultMessage;
    this.level = level;
  }
}
