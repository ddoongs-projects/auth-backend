package com.ddoongs.auth.api.docs;

import static com.ddoongs.auth.domain.shared.CoreErrorCode.EXPIRED_TOKEN;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.INVALID_TOKEN;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.MEMBER_NOT_FOUND;
import static com.ddoongs.auth.domain.shared.CoreErrorCode.PASSWORD_MISMATCH;
import static com.ddoongs.auth.restdocs.RestdocsUtils.errorCodes;
import static com.ddoongs.auth.restdocs.RestdocsUtils.errorWithCause;
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
                fieldWithPath("refreshToken").description("JWT refresh token")),
            errorCodes(
                errorWithCause(PASSWORD_MISMATCH, "비밀번호가 다를 때 발생"),
                errorWithCause(MEMBER_NOT_FOUND, "회원이 존재하지 않으면 발생"))));
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
                fieldWithPath("refreshToken").description("JWT refresh token")),
            errorCodes(
                errorWithCause(INVALID_TOKEN, "토큰이 올바르지 않을 때 발생"),
                errorWithCause(EXPIRED_TOKEN, "토큰이 만료되었을 때 발생"))));
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
                fieldWithPath("refreshToken").description("JWT refresh token")),
            errorCodes(
                errorWithCause(INVALID_TOKEN, "토큰이 올바르지 않을 때 발생"),
                errorWithCause(EXPIRED_TOKEN, "토큰이 만료되었을 때 발생"))));
  }
}
