package com.ddoongs.auth.api.docs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.ddoongs.auth.api.auth.AuthApi;
import com.ddoongs.auth.api.auth.MemberLoginRequest;
import com.ddoongs.auth.api.auth.MemberLogoutRequest;
import com.ddoongs.auth.api.auth.ReissueRequest;
import com.ddoongs.auth.api.auth.RenewRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureRestDocs
@WebMvcTest(AuthApi.class)
@Tag("restdocs")
class AuthApiDocsTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
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
            "auth-login",
            requestFields(
                fieldWithPath("email").description("이메일"),
                fieldWithPath("password").description("비밀번호")),
            responseFields(
                fieldWithPath("accessToken").description("JWT access token"),
                fieldWithPath("refreshToken").description("JWT refresh token"))));
  }

  @DisplayName("토큰 재발급 API 문서 생성")
  @Test
  void reissue() throws Exception {
    final var request = new ReissueRequest("sample.refresh.token");

    given(tokenService.reissue(any()))
        .willReturn(new TokenPair(
            "sample.new.access.token",
            new RefreshToken(
                UUID.randomUUID().toString(),
                "test@email.com",
                Instant.now().plus(Duration.ofDays(7)),
                "sample.refresh.token")));

    assertThat(mvc.post()
            .uri("/auth/reissue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "auth-reissue",
            requestFields(fieldWithPath("refreshToken").description("JWT refresh token")),
            responseFields(
                fieldWithPath("accessToken").description("new JWT access token"),
                fieldWithPath("refreshToken").description("JWT refresh token"))));
  }

  @DisplayName("토큰 갱신 API 문서 생성")
  @Test
  void renew() throws Exception {
    final var request = new RenewRequest("sample.refresh.token");

    given(tokenService.renew(any()))
        .willReturn(new TokenPair(
            "sample.access.token",
            new RefreshToken(
                UUID.randomUUID().toString(),
                "test@email.com",
                Instant.now().plus(Duration.ofDays(7)),
                "sample.new.refresh.token")));

    assertThat(mvc.post()
            .uri("/auth/renew")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "auth-renew",
            requestFields(fieldWithPath("refreshToken").description("JWT refresh token")),
            responseFields(
                fieldWithPath("accessToken").description("JWT access token"),
                fieldWithPath("refreshToken").description("new JWT refresh token"))));
  }

  @DisplayName("로그아웃 API 문서 생성")
  @Test
  void logout() throws Exception {
    final var request = new MemberLogoutRequest("sample.access.token", "sample.refresh.token");

    assertThat(mvc.post()
            .uri("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .exchange())
        .hasStatusOk()
        .apply(document(
            "auth-logout",
            requestFields(
                fieldWithPath("accessToken").description("JWT access token"),
                fieldWithPath("refreshToken").description("JWT refresh token"))));
  }
}
