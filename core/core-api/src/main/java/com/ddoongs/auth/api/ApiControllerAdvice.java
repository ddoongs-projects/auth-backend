package com.ddoongs.auth.api;

import com.ddoongs.auth.domain.shared.BusinessException;
import com.ddoongs.auth.domain.verification.VerificationCooldownException;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

  private ProblemDetail getProblemDetail(HttpStatus httpStatus, Exception e) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, e.getMessage());

    problemDetail.setProperty("timestamp", LocalDateTime.now());
    problemDetail.setProperty("exception", e.getClass().getSimpleName());
    return problemDetail;
  }

  @ExceptionHandler(VerificationCooldownException.class)
  public ResponseEntity<ProblemDetail> handleVerificationCooldownException(
      VerificationCooldownException e) {
    ProblemDetail problemDetail = getProblemDetail(HttpStatus.BAD_REQUEST, e);
    problemDetail.setProperty("remainSeconds", e.getRemainCooldown().getSeconds());
    return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException e) {
    ProblemDetail problemDetail = getProblemDetail(HttpStatus.BAD_REQUEST, e);
    return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleException(Exception e) {
    ProblemDetail problemDetail = getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
    return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
  }
}
