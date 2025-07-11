package com.ddoongs.auth.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

  private final String result;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T data;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final ErrorResponse error;

  private ApiResponse(String result, T data, ErrorResponse error) {
    this.result = result;
    this.data = data;
    this.error = error;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("SUCCESS", data, null);
  }

  public static ApiResponse<?> error(String code, String message) {
    return new ApiResponse<>("ERROR", null, new ErrorResponse(code, message));
  }

  @Getter
  private static class ErrorResponse {
    private final String code;
    private final String message;

    private ErrorResponse(String code, String message) {
      this.code = code;
      this.message = message;
    }
  }
}
