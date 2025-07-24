package com.ddoongs.auth.api.docs.controller;

import com.ddoongs.auth.api.ApiErrorCode;
import com.ddoongs.auth.domain.shared.CoreErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorCodeController {

  @GetMapping("/error-codes")
  public Map<String, String> errorCode() {
    Map<String, String> result = new HashMap<>();
    for (var code : ApiErrorCode.values()) {
      result.put(code.name(), code.getDefaultMessage());
    }

    for (var code : CoreErrorCode.values()) {
      result.put(code.name(), code.getDefaultMessage());
    }

    return result;
  }
}
