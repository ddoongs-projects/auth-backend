package com.ddoongs.auth.api.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddoongs.auth.api.docs.controller.MockOAuth2Controller;
import com.ddoongs.auth.restdocs.RestdocsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * OAuth2 로그인 플로우 API 문서화 테스트
 *
 * <p>OAuth2 로그인 플로우:</p>
 * <ol>
 *   <li>클라이언트가 {@code /oauth2/authorization/{provider}}로 GET 요청</li>
 *   <li>Spring Security가 OAuth2 제공자의 인증 페이지로 리다이렉트</li>
 *   <li>사용자가 OAuth2 제공자에서 인증 완료</li>
 *   <li>OAuth2 제공자가 {@code /login/oauth2/code/{provider}}로 콜백</li>
 *   <li>OAuth2AuthenticationSuccessHandler가 처리하여 authCode 생성</li>
 *   <li>프론트엔드로 리다이렉트 (authCode 포함)</li>
 *   <li>프론트엔드가 {@code /auth/token/exchange}로 authCode를 전송하여 JWT 토큰 획득</li>
 * </ol>
 */
@WebMvcTest(MockOAuth2Controller.class)
class OAuth2ApiDocsTest extends RestdocsTest {

  @Autowired
  private MockMvc mockMvc;

  @DisplayName("OAuth2 로그인 시작")
  @Test
  void oauth2Authorization() throws Exception {
    mockMvc
        .perform(get("/oauth2/authorization/{provider}", "google"))
        .andExpect(status().isFound()) // 302 Found 검증
        .andDo(document(
            "oauth2-authorization",
            pathParameters(parameterWithName("provider")
                .description("OAuth2 제공자 (google, naver, kakao 지원)"))));
  }
}
