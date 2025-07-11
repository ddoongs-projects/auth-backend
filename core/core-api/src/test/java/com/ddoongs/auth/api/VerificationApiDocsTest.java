package com.ddoongs.auth.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.domain.support.VerificationFixture;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureRestDocs
@WebMvcTest(VerificationApi.class)
@Tag("restdocs")
class VerificationApiDocsTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  private VerificationCodeGenerator verificationCodeGenerator;

  @MockitoBean
  private VerificationService verificationService;

  @BeforeEach
  void setup() {
    verificationCodeGenerator = TestFixture.verificationCodeGenerator();
  }

  @DisplayName("인증번호 발급 API 문서 생성")
  @Test
  void issue() throws Exception {
    final var request =
        new CreateVerificationRequest("test@email.com", VerificationPurpose.REGISTER);

    given(verificationService.issue(request.toCommand()))
        .willReturn(VerificationFixture.verification(verificationCodeGenerator));

    assertThat(mvc.post()
            .uri("/verifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "verification-issue",
            requestFields(
                fieldWithPath("email").description("이메일"),
                fieldWithPath("purpose")
                    .description("인증 목적 (REGISTER: 회원가입, RESET_PASSWORD: 비밀번호 초기화)")),
            responseFields(fieldWithPath("verificationId").description("인증 식별자"))));
  }
}
