package com.ddoongs.auth.domain.shared;

import lombok.Getter;

/**
 * 모든 비즈니스 예외의 최상위 추상 클래스입니다.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

  private final CoreErrorCode code;

  public BusinessException(CoreErrorCode code, Object... args) {
    super(String.format(code.getDefaultMessage(), args));
    this.code = code;
  }
}
