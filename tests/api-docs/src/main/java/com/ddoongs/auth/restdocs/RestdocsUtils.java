package com.ddoongs.auth.restdocs;

import com.ddoongs.auth.domain.shared.CoreErrorCode;

public class RestdocsUtils {

  private RestdocsUtils() {}

  public static ErrorCodeSnippet errorCodesWithCause(ApiErrorWithCause... apiErrorWithCauses) {
    return new ErrorCodeSnippet(apiErrorWithCauses);
  }

  public static ApiErrorWithCause errorWithCause(CoreErrorCode code, String cause) {
    return new ApiErrorWithCause(code.name(), code.getDefaultMessage(), cause);
  }
}
