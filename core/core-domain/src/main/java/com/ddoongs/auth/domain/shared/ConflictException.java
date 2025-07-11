package com.ddoongs.auth.domain.shared;

/**
 * 리소스 충돌(중복 등) 시 발생하는 예외의 최상위 클래스입니다.
 */
public class ConflictException extends BusinessException {

  public ConflictException(Object... args) {
    super(CoreErrorCode.CONFLICT, args);
  }
}
