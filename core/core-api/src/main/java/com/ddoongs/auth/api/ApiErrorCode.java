package com.ddoongs.auth.api;

import lombok.Getter;

@Getter
public enum ApiErrorCode {
  INVALID_REQUEST("잘못된 요청입니다."),
  NOT_FOUND("리소스를 찾을 수 없습니다."),
  COMMON_ERROR("내부 서버 오류입니다.");

  private final String defaultMessage;

  ApiErrorCode(String defaultMessage) {
    this.defaultMessage = defaultMessage;
  }
}
