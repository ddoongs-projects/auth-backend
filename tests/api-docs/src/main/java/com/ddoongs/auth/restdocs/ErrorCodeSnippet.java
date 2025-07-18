package com.ddoongs.auth.restdocs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

public class ErrorCodeSnippet extends TemplatedSnippet {

  private final ApiErrorWithCause[] apiErrorWithCauses; // ApiErrorDetail 배열로 변경

  public ErrorCodeSnippet(ApiErrorWithCause... apiErrorWithCauses) {
    super("error-codes", null);
    this.apiErrorWithCauses = apiErrorWithCauses;
  }

  @Override
  protected Map<String, Object> createModel(Operation operation) {
    Map<String, Object> model = new HashMap<>();

    List<Map<String, String>> errorCodesWithCause = Arrays.stream(this.apiErrorWithCauses)
        .map(detail -> Map.of(
            "code", detail.code(),
            "message", detail.message(),
            "cause", detail.cause()))
        .toList();

    model.put("errorCodes", errorCodesWithCause);

    return model;
  }
}
