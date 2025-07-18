package com.ddoongs.auth.restdocs;

import com.ddoongs.auth.domain.shared.CoreErrorCode;

public class RestdocsUtils {

  private RestdocsUtils() {}

  public static ErrorCodeSnippet errorCodes(ApiErrorDetail... apiErrorDetails) {
    return new ErrorCodeSnippet(apiErrorDetails);
  }

  public static ApiErrorDetail errorWithCause(CoreErrorCode code, String cause) {
    return new ApiErrorDetail(code, cause);
  }
}
