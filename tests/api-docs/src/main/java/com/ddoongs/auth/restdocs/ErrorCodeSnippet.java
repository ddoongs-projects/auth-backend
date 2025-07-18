package com.ddoongs.auth.restdocs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

public class ErrorCodeSnippet extends TemplatedSnippet {

  private final ApiErrorDetail[] apiErrorDetails; // ApiErrorDetail 배열로 변경

  public ErrorCodeSnippet(ApiErrorDetail... apiErrorDetails) {
    super("error-codes", null);
    this.apiErrorDetails = apiErrorDetails;
  }

  @Override
  protected Map<String, Object> createModel(Operation operation) {
    Map<String, Object> model = new HashMap<>();

    List<Map<String, String>> errorCodesWithCause = Arrays.stream(this.apiErrorDetails)
        .map(detail -> Map.of(
            "code", detail.code().name(),
            "message", detail.code().getDefaultMessage(),
            "cause", detail.cause()))
        .toList();

    model.put("errorCodes", errorCodesWithCause);

    return model;
  }
}
