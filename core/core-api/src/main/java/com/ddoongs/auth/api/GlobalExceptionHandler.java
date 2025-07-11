package com.ddoongs.auth.api;

import com.ddoongs.auth.domain.shared.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.warn("MethodArgumentNotValidException", e);
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    return ApiResponse.error("INVALID_INPUT", errorMessage);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BusinessException.class)
  public ApiResponse<?> handleBusinessException(BusinessException e) {
    log.warn("BusinessException", e);
    // BusinessException의 메시지를 그대로 사용하거나, 에러 코드 시스템을 도입할 수 있습니다.
    return ApiResponse.error("BUSINESS_ERROR", e.getMessage());
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ApiResponse<?> handleException(Exception e) {
    log.error("Exception", e);
    return ApiResponse.error("INTERNAL_ERROR", "서버에 오류가 발생했습니다.");
  }
}
