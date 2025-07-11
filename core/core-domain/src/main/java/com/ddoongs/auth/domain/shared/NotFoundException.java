package com.ddoongs.auth.domain.shared;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외의 최상위 클래스입니다.
 */
public class NotFoundException extends BusinessException {

  public NotFoundException(CoreErrorCode code, Object... args) {
    super(code, args);
  }

  public NotFoundException(Object... args) {
    super(CoreErrorCode.NOT_FOUND, args);
  }
}
