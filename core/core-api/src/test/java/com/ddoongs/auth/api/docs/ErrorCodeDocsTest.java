package com.ddoongs.auth.api.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import com.ddoongs.auth.api.ApiErrorCode;
import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.restdocs.AllErrorCodeSnippet;
import com.ddoongs.auth.restdocs.ApiError;
import com.ddoongs.auth.restdocs.RestdocsTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ErrorCodeController.class)
public class ErrorCodeDocsTest extends RestdocsTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void error_codes() throws Exception {
    assertThat(mvc.get()
            .uri("/error-codes")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange())
        .hasStatusOk()
        .apply(document("error-codes", new AllErrorCodeSnippet(collectErrorCodes())));
  }

  private ApiError[] collectErrorCodes() {
    List<ApiError> res = new ArrayList<>();
    for (var code : ApiErrorCode.values()) {
      res.add(new ApiError(code.name(), code.getDefaultMessage()));
    }

    for (var code : CoreErrorCode.values()) {
      res.add(new ApiError(code.name(), code.getDefaultMessage()));
    }

    return res.toArray(ApiError[]::new);
  }
}
