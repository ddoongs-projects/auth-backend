package com.ddoongs.auth.api.member;

import static com.ddoongs.auth.domain.util.AssertThatUtils.equalsTo;
import static com.ddoongs.auth.domain.util.AssertThatUtils.notNull;
import static org.assertj.core.api.Assertions.assertThat;

import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.shared.CoreErrorCode;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.support.FakeVerificationCodeGenerator;
import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class MemberApiTest {

  @Autowired
  private MockMvcTester mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private VerificationService verificationService;

  @Autowired
  private FakeVerificationCodeGenerator verificationCodeGenerator;

  @Qualifier("passwordEncoder") @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setup() {
    verificationCodeGenerator.setFixedCode("123456");
  }

  @DisplayName("회원을 등록한다.")
  @Test
  void register() throws Exception {
    String fixedCode = "123456";
    String email = "test@email.com";
    String password = "123qwe!@#";

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER));

    verificationService.verify(verification.getId(), new VerificationCode(fixedCode));

    final var request = new MemberRegisterRequest(email, password, verification.getId());

    final var result = mvc.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatusOk()
        .bodyJson()
        .hasPathSatisfying("$.memberId", notNull())
        .hasPathSatisfying("$.email", equalsTo(email));

    final var response = objectMapper.readValue(
        result.getResponse().getContentAsString(), MemberRegisterResponse.class);

    Member member = memberRepository.find(response.memberId()).orElseThrow();

    assertThat(member.getId()).isNotNull();
    assertThat(member.getEmail().address()).isEqualTo(email);
    assertThat(member.getPassword().getPasswordHash()).isNotNull();
  }

  @DisplayName("중복된 이메일로 회원 등록을 할 수 없다.")
  @Test
  void registerFailDupliatedEmail() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

    memberRepository.save(MemberFixture.member(email, passwordEncoder));

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    final var request = new MemberRegisterRequest(email, password, verification.getId());

    final var result = mvc.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.DUPLICATED_EMAIL.toString()));
  }

  @DisplayName("다른 목적으로 발급한 인증으로는 회원 등록이 실패한다.")
  @Test
  void registerFailDifferentPurpose() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.RESET_PASSWORD));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    final var request = new MemberRegisterRequest(email, password, verification.getId());

    final var result = mvc.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.VERIFICATION_MISMATCH.toString()));
  }

  @DisplayName("인증을 완료하지 않으면 회원 등록이 실패한다.")
  @Test
  void registerFailNotVerified() throws Exception {
    String fixedCode = "123456";
    String email = "test@email.com";
    String password = "123qwe!@#";

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER));

    final var request = new MemberRegisterRequest(email, password, verification.getId());

    final var result = mvc.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.VERIFICATION_NOT_COMPLETED.toString()));
  }

  @DisplayName("다른 이메일로 발급한 요청으로는 회원 등록이 실패한다.")
  @Test
  void registerFail() throws Exception {
    String fixedCode = "123456";
    String email = "test@email.com";
    String password = "123qwe!@#";

    Verification verification = verificationService.issue(
        new CreateVerification(new Email("notSame@email.com"), VerificationPurpose.REGISTER));

    final var request = new MemberRegisterRequest(email, password, verification.getId());

    final var result = mvc.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.VERIFICATION_MISMATCH.toString()));
  }
}
