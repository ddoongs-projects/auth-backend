package com.ddoongs.auth.api.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.ddoongs.auth.api.auth.MemberLoginRequest;
import com.ddoongs.auth.api.member.MemberApi;
import com.ddoongs.auth.domain.token.RefreshToken;
import com.ddoongs.auth.domain.token.TokenPair;
import com.ddoongs.auth.domain.token.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureRestDocs
@WebMvcTest(MemberApi.class)
@Tag("restdocs")
class AuthApiDocsTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TokenService tokenService;

  @DisplayName("로그인 API 문서 생성")
  @Test
  void login() throws Exception {
    final var request = new MemberLoginRequest("test@email.com", "123asd!@#");

    given(tokenService.login(any()))
        .willReturn(new TokenPair(
            "sample.access.token",
            new RefreshToken(
                UUID.randomUUID().toString(),
                "test@email.com",
                Instant.now().plus(Duration.ofDays(7)),
                "sample.refresh.token")));

    assertThat(mvc.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "member-login",
            requestFields(
                fieldWithPath("email").description("이메일"),
                fieldWithPath("password").description("비밀번호")),
            responseFields(
                fieldWithPath("accessToken").description("JWT access token"),
                fieldWithPath("refreshToken").description("JWT refresh token"))));
  }
}
