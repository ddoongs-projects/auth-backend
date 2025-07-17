package com.ddoongs.auth.api.auth;

import static com.ddoongs.auth.domain.util.AssertThatUtils.equalsTo;
import static com.ddoongs.auth.domain.util.AssertThatUtils.notNull;
import static org.assertj.core.api.Assertions.assertThat;

import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.member.RegisterMember;
import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.support.FakeClock;
import com.ddoongs.auth.domain.support.FakeVerificationCodeGenerator;
import com.ddoongs.auth.domain.token.BlacklistTokenRepository;
import com.ddoongs.auth.domain.token.LoginMember;
import com.ddoongs.auth.domain.token.RefreshToken;
import com.ddoongs.auth.domain.token.RefreshTokenRepository;
import com.ddoongs.auth.domain.token.TokenPair;
import com.ddoongs.auth.domain.token.TokenProvider;
import com.ddoongs.auth.domain.token.TokenService;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@Import(AuthTestConfiguration.class)
@SpringBootTest
class AuthApiTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private VerificationService verificationService;

  @Autowired
  private FakeVerificationCodeGenerator verificationCodeGenerator;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private MemberService memberService;

  @Autowired
  private TokenProvider tokenProvider;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private FakeClock fakeClock;

  @Qualifier("blacklistTokenRepository") @Autowired
  private BlacklistTokenRepository blacklistTokenRepository;

  @BeforeEach
  void setup() {
    fakeClock.resetToNow();
  }

  @DisplayName("로그인을 진행한다.")
  @Test
  void login() throws JsonProcessingException, UnsupportedEncodingException {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    final var request = new MemberLoginRequest(email, password);

    final var result = mvc.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatusOk()
        .bodyJson()
        .hasPathSatisfying("$.accessToken", notNull())
        .hasPathSatisfying("$.refreshToken", notNull());

    final var response =
        objectMapper.readValue(result.getResponse().getContentAsString(), TokenResponse.class);

    String jti = tokenProvider.extractJti(response.refreshToken());
    RefreshToken refreshToken = refreshTokenRepository.find(jti).orElseThrow();

    assertThat(refreshToken.token()).isNotNull();
  }

  @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다.")
  @Test
  void loginFailPasswordMismatch() throws JsonProcessingException {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    final var request = new MemberLoginRequest(email, password + "a'");

    final var result = mvc.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.PASSWORD_MISMATCH.toString()));
  }

  @DisplayName("회원이 존재하지 않으면 않으면 로그인에 실패한다.")
  @Test
  void loginFailMemberNotFound() throws JsonProcessingException {
    String email = "test@email.com";
    String password = "123qwe!@#";

    final var request = new MemberLoginRequest(email, password + "a'");

    final var result = mvc.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.NOT_FOUND)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.MEMBER_NOT_FOUND.toString()));
  }

  private void registerMember(String email, String password) {
    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER));
    verificationService.verify(verification.getId(), new VerificationCode("123456"));
    memberService.register(new RegisterMember(email, password), verification.getId());
  }

  @DisplayName("토큰을 reissue 받는다.")
  @Test
  void reissue() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    final var tokenPair = tokenService.login(new LoginMember(email, password));

    final var request = new ReissueRequest(tokenPair.refreshToken().token());

    final var result = mvc.post()
        .uri("/auth/reissue")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatusOk()
        .bodyJson()
        .hasPathSatisfying("$.accessToken", notNull())
        .hasPathSatisfying("$.refreshToken", notNull());
  }

  @DisplayName("잘못된 리프레시 토큰으로는 reissue에 실패한다.")
  @Test
  void reissueFail() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    final var tokenPair = tokenService.login(new LoginMember(email, password));

    fakeClock.plus(Duration.ofDays(50));

    final var request = new ReissueRequest(tokenPair.refreshToken().token());

    final var result = mvc.post()
        .uri("/auth/reissue")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.EXPIRED_TOKEN.toString()));
  }

  @DisplayName("만료된 리프레시 토큰으로는 reissue에 실패한다.")
  @Test
  void reissueFailWithExpiredToken() throws Exception {
    final var request = new ReissueRequest("invalid-token");

    final var result = mvc.post()
        .uri("/auth/reissue")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.INVALID_TOKEN.toString()));
  }

  @DisplayName("리프레시 토큰을 renew한다.")
  @Test
  void renew() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    final var tokenPair = tokenService.login(new LoginMember(email, password));

    fakeClock.plus(Duration.ofDays(6));

    final var request = new RenewRequest(tokenPair.refreshToken().token());

    final var result = mvc.post()
        .uri("/auth/renew")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatusOk()
        .bodyJson()
        .hasPathSatisfying("$.accessToken", notNull())
        .hasPathSatisfying("$.refreshToken", notNull());
  }

  @DisplayName("threshold 때는 renew에 실패한다.")
  @Test
  void renewFailWithThreshold() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    final var tokenPair = tokenService.login(new LoginMember(email, password));

    final var request = new RenewRequest(tokenPair.refreshToken().token());

    final var result = mvc.post()
        .uri("/auth/renew")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .bodyJson()
        .hasPathSatisfying(
            "$.code", equalsTo(CoreErrorCode.TOKEN_RENEWAL_CONDITION_NOT_MET.toString()));
  }

  @DisplayName("잘못된 리프레시 토큰으로는 renew에 실패한다.")
  @Test
  void renewFail() throws Exception {
    final var request = new RenewRequest("invalid-token");

    final var result = mvc.post()
        .uri("/auth/renew")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.UNAUTHORIZED)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.INVALID_TOKEN.toString()));
  }

  @DisplayName("로그아웃을 진행한다.")
  @Test
  void logout() throws JsonProcessingException {
    String email = "test@email.com";
    String password = "123qwe!@#";

    registerMember(email, password);

    TokenPair tokenPair = tokenService.login(new LoginMember(email, password));

    final var request = new MemberLogoutRequest(
        tokenPair.accessToken(), tokenPair.refreshToken().token());

    final var result = mvc.post()
        .uri("/auth/logout")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result).hasStatusOk();

    String accessJti = tokenProvider.extractJti(tokenPair.accessToken());
    String refreshJti = tokenPair.refreshToken().jti();
    assertThat(blacklistTokenRepository.exists(accessJti)).isTrue();
    assertThat(blacklistTokenRepository.exists(refreshJti)).isTrue();
  }
}
