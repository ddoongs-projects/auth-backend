package com.ddoongs.auth.restdocs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;

public class AllErrorCodeSnippet extends TemplatedSnippet {

  private final ApiError[] apiErrors; // ApiErrorDetail 배열로 변경

  public AllErrorCodeSnippet(ApiError... apiErrors) {
    super("all-error-codes", null);
    this.apiErrors = apiErrors;
  }

  @Override
  protected Map<String, Object> createModel(Operation operation) {
    Map<String, Object> model = new HashMap<>();

    List<Map<String, String>> errorCodesWithCause = Arrays.stream(this.apiErrors)
        .map(detail -> Map.of(
            "code", detail.code(),
            "message", detail.message()))
        .toList();

    model.put("errorCodes", errorCodesWithCause);

    return model;
  }
}
