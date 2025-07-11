package com.ddoongs.auth.domain.shared;

/**
 * 도메인 검증 실패 시 발생하는 예외의 최상위 클래스입니다.
 */
public class ValidationException extends BusinessException {

  public ValidationException(Object... args) {
    super(CoreErrorCode.INVALID_REQUEST, args);
  }
}
