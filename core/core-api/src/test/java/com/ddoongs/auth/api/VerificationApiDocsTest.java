package com.ddoongs.auth.api;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(AuthTestConfiguration.class)
@SpringBootTest
class VerificationApiDocsTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @DisplayName("인증번호 발급 API 문서 생성")
  @Test
  void issue() throws Exception {
    final var request =
        new CreateVerificationRequest("test@email.com", VerificationPurpose.REGISTER);

    mockMvc
        .perform(post("/verifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(document(
            "verification-issue",
            requestFields(
                fieldWithPath("email").description("이메일"),
                fieldWithPath("purpose")
                    .description("인증 목적 (REGISTER: 회원가입, RESET_PASSWORD: 비밀번호 초기화)"))));
  }
}
