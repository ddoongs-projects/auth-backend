package com.ddoongs.auth.api;

import com.ddoongs.auth.domain.shared.BusinessException;
import com.ddoongs.auth.domain.shared.ConflictException;
import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.NotFoundException;
import com.ddoongs.auth.domain.shared.UnauthorizedException;
import com.ddoongs.auth.domain.shared.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalApiAdvice {

  /**
   * 클라이언트 요청 오류(잘못된 메서드, 미디어 타입, 파라미터/바인딩/검증 오류 등)를 처리합니다.
   */
  @ExceptionHandler({
    HttpRequestMethodNotSupportedException.class,
    HttpMediaTypeNotSupportedException.class,
    HttpMediaTypeNotAcceptableException.class,
    MissingPathVariableException.class,
    MissingServletRequestParameterException.class,
    MissingServletRequestPartException.class,
    ServletRequestBindingException.class,
    MethodArgumentNotValidException.class,
    HandlerMethodValidationException.class,
    MethodValidationException.class,
    TypeMismatchException.class,
    MethodArgumentTypeMismatchException.class,
    MaxUploadSizeExceededException.class,
    HttpMessageNotReadableException.class,
    ErrorResponseException.class
  })
  public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception ex) {
    log.warn("Invalid request: ", ex);

    ApiErrorCode errorCode = ApiErrorCode.INVALID_REQUEST;
    ErrorResponse response = new ErrorResponse(errorCode.toString(), errorCode.getDefaultMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * 존재하지 않는 리소스(잘못된 URL 매핑 등)에 대한 요청을 처리합니다.
   */
  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
    log.info("Not found: ", ex);

    ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
    ErrorResponse response = new ErrorResponse(errorCode.toString(), errorCode.getDefaultMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * 서버 내부 오류(비동기 처리 오류, 변환 실패 등) 및 알 수 없는 예외를 처리합니다.
   */
  @ExceptionHandler({
    AsyncRequestTimeoutException.class,
    AsyncRequestNotUsableException.class,
    ConversionNotSupportedException.class,
    HttpMessageNotWritableException.class,
    Exception.class,
  })
  public ResponseEntity<ErrorResponse> handleServerError(Exception ex) {
    log.error("Server error: ", ex);
    ApiErrorCode errorCode = ApiErrorCode.COMMON_ERROR;
    ErrorResponse response = new ErrorResponse(errorCode.toString(), errorCode.getDefaultMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  /**
   * 최상위 비즈니스 오류를 처리합니다.
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    logException(ex);
    return buildResponse(ex, HttpStatus.BAD_REQUEST);
  }

  /**
   * 리소스를 찾을 수 없을 때(404)
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
    logException(ex);
    return buildResponse(ex, HttpStatus.NOT_FOUND);
  }

  /**
   * 요청 검증 실패 또는 리소스 충돌(400)
   */
  @ExceptionHandler({ValidationException.class, ConflictException.class})
  public ResponseEntity<ErrorResponse> handleValidationException(BusinessException ex) {
    logException(ex);
    return buildResponse(ex, HttpStatus.BAD_REQUEST);
  }

  /**
   * 인증 실패 (401)
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(BusinessException ex) {
    logException(ex);
    return buildResponse(ex, HttpStatus.UNAUTHORIZED);
  }

  private ResponseEntity<ErrorResponse> buildResponse(BusinessException ex, HttpStatus status) {
    CoreErrorCode code = ex.getCode();
    ErrorResponse body = new ErrorResponse(code.toString(), ex.getMessage());

    return ResponseEntity.status(status).body(body);
  }

  private void logException(BusinessException ex) {
    switch (ex.getCode().getLevel()) {
      case ERROR -> log.error(ex.getMessage(), ex);
      case WARN -> log.warn(ex.getMessage(), ex);
      case INFO -> log.info(ex.getMessage(), ex);
    }
  }
}
