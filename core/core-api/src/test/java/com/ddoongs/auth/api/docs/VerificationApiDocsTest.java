package com.ddoongs.auth.api.docs;

import static com.ddoongs.auth.domain.shared.CoreErrorCode.INVALID_VERIFICATION_CODE;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.VERIFICATION_ALREADY_COMPLETED;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.VERIFICATION_COOLDOWN;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.VERIFICATION_NOT_FOUND;
import static com.ddoongs.auth.restdocs.RestdocsUtils.errorCodes;
import static com.ddoongs.auth.restdocs.RestdocsUtils.errorWithCause;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.ddoongs.auth.api.verification.CreateVerificationRequest;
import com.ddoongs.auth.api.verification.VerificationApi;
import com.ddoongs.auth.api.verification.VerifyVerificationRequest;
import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.domain.support.VerificationFixture;
import com.ddoongs.auth.domain.verification.VerificationCodeGenerator;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
            responseFields(fieldWithPath("verificationId").description("인증 식별자")),
            errorCodes(errorWithCause(VERIFICATION_COOLDOWN, "인증 쿨다운 시간 이내에 시도하면 발생"))));
  }

  @DisplayName("인증번호 인증 API 문서 생성")
  @Test
  void verify() throws Exception {

    UUID verificationId = UUID.randomUUID();
    String code = "123456";
    final var request = new VerifyVerificationRequest(verificationId, code);

    assertThat(mvc.post()
            .uri("/verifications/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "verification-verify",
            requestFields(
                fieldWithPath("verificationId").description("인증코드 식별자"),
                fieldWithPath("code").description("인증 코드")),
            errorCodes(
                errorWithCause(INVALID_VERIFICATION_CODE, "인증 코드가 다르면 발생"),
                errorWithCause(VERIFICATION_NOT_FOUND, "인증 코드가 존재하지 않으면 발생"),
                errorWithCause(VERIFICATION_ALREADY_COMPLETED, "인증 코드가 이미 인증 완료 되었으면 발생"))));
  }
}
