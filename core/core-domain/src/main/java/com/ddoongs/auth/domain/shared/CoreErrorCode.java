package com.ddoongs.auth.domain.shared;

import static com.ddoongs.auth.domain.shared.CoreErrorLevel.WARN;

import lombok.Getter;

@Getter
public enum CoreErrorCode {
  INVALID_REQUEST(WARN, "잘못된 요청입니다."),
  NOT_FOUND(WARN, "리소스를 찾을 수 없습니다."),
  CONFLICT(WARN, "이미 존재하는 리소스입니다."),
  VERIFICATION_COOLDOWN(WARN, "인증 발급 요청 간격이 너무 짧습니다. %d초 후에 시도해 주세요."),
  VERIFICATION_ALREADY_COMPLETED(WARN, "인증이 이미 완료되었습니다."),
  INVALID_VERIFICATION_CODE(WARN, "인증코드가 일치하지 않습니다."),
  VERIFICATION_NOT_FOUND(WARN, "존재하지 않는 인증입니다."),
  VERIFICATION_MISMATCH(WARN, "요청 대상과 일치하는 인증 정보가 아닙니다."),
  VERIFICATION_NOT_COMPLETED(WARN, "인증이 완료되지 않았습니다."),
  DUPLICATED_EMAIL(WARN, "중복된 이메일입니다.: %s");

  private final String defaultMessage;
  private final CoreErrorLevel level;

  CoreErrorCode(CoreErrorLevel level, String defaultMessage) {
    this.defaultMessage = defaultMessage;
    this.level = level;
  }
}
