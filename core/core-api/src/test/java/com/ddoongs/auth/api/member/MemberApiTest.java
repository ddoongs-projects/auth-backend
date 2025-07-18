package com.ddoongs.auth.api.member;

import static com.ddoongs.auth.domain.util.AssertThatUtils.equalsTo;
import static com.ddoongs.auth.domain.util.AssertThatUtils.notNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.MemberService;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.member.PasswordMismatchException;
import com.ddoongs.auth.domain.member.RegisterMember;
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
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureMockMvc
@Import(AuthTestConfiguration.class)
@SpringBootTest
class MemberApiTest {

  @MockitoBean
  DateTimeProvider dateTimeProvider;

  @MockitoSpyBean
  AuditingHandler auditingHandler;

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

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MemberService memberService;

  @BeforeEach
  void setup() {
    verificationCodeGenerator.setFixedCode("123456");
    given(dateTimeProvider.getNow()).willReturn(Optional.of(LocalDateTime.now()));
    auditingHandler.setDateTimeProvider(dateTimeProvider);
  }

  @DisplayName("회원을 등록한다.")
  @Test
  void register() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";

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

  @DisplayName("비밀번호를 초기화한다.")
  @Test
  void resetPassword() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";
    String newPassword = "456rty$%^";

    registerMember(email, password);

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.RESET_PASSWORD));
    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    final var request = new ResetPasswordRequest(email, newPassword, verification.getId());

    final var result = mvc.post()
        .uri("/members/reset-password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result).hasStatusOk();

    Member member = memberRepository.findByEmail(new Email(email)).orElseThrow();

    assertThatThrownBy(() -> member.validatePassword(password, passwordEncoder))
        .isInstanceOf(PasswordMismatchException.class);
    assertThatCode(() -> member.validatePassword(newPassword, passwordEncoder))
        .doesNotThrowAnyException();
  }

  @DisplayName("인증이 완료되지 않으면 비밀번호를 초기화에 실패한다.")
  @Test
  void resetPasswordNotVerifiedVerification() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";
    String newPassword = "456rty$%^";

    registerMember(email, password);

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.RESET_PASSWORD));

    final var request = new ResetPasswordRequest(email, newPassword, verification.getId());

    final var result = mvc.post()
        .uri("/members/reset-password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.VERIFICATION_NOT_COMPLETED.toString()));

    Member member = memberRepository.findByEmail(new Email(email)).orElseThrow();

    assertThatThrownBy(() -> member.validatePassword(newPassword, passwordEncoder))
        .isInstanceOf(PasswordMismatchException.class);
    assertThatCode(() -> member.validatePassword(password, passwordEncoder))
        .doesNotThrowAnyException();
  }

  @DisplayName("해당 회원에 대한 인증이 아니면 비밀번호를 초기화에 실패한다.")
  @Test
  void resetPasswordDifferentEmail() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";
    String newPassword = "456rty$%^";

    registerMember(email, password);

    Verification verification = verificationService.issue(new CreateVerification(
        new Email("different@email.com"), VerificationPurpose.RESET_PASSWORD));

    final var request = new ResetPasswordRequest(email, newPassword, verification.getId());

    final var result = mvc.post()
        .uri("/members/reset-password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.VERIFICATION_MISMATCH.toString()));

    Member member = memberRepository.findByEmail(new Email(email)).orElseThrow();

    assertThatThrownBy(() -> member.validatePassword(newPassword, passwordEncoder))
        .isInstanceOf(PasswordMismatchException.class);
    assertThatCode(() -> member.validatePassword(password, passwordEncoder))
        .doesNotThrowAnyException();
  }

  @DisplayName("해당 목적에 대한 인증이 아니면 비밀번호를 초기화에 실패한다.")
  @Test
  void resetPasswordDifferentPurpose() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";
    String newPassword = "456rty$%^";

    given(dateTimeProvider.getNow()).willReturn(Optional.of(LocalDateTime.now().minusDays(1)));

    registerMember(email, password);

    given(dateTimeProvider.getNow()).willReturn(Optional.of(LocalDateTime.now()));

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    final var request = new ResetPasswordRequest(email, newPassword, verification.getId());

    final var result = mvc.post()
        .uri("/members/reset-password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
        .exchange();

    assertThat(result)
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .hasPathSatisfying("$.code", equalsTo(CoreErrorCode.VERIFICATION_MISMATCH.toString()));

    Member member = memberRepository.findByEmail(new Email(email)).orElseThrow();

    assertThatThrownBy(() -> member.validatePassword(newPassword, passwordEncoder))
        .isInstanceOf(PasswordMismatchException.class);
    assertThatCode(() -> member.validatePassword(password, passwordEncoder))
        .doesNotThrowAnyException();
  }

  @DisplayName("해당 이메일을 가진 사용자가 없다면 비밀번호를 초기화에 실패한다.")
  @Test
  void resetPasswordNotFoundMember() throws Exception {
    String email = "test@email.com";
    String password = "123qwe!@#";
    String newPassword = "456rty$%^";

    Verification verification = verificationService.issue(
        new CreateVerification(new Email(email), VerificationPurpose.RESET_PASSWORD));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    final var request = new ResetPasswordRequest(email, newPassword, verification.getId());

    final var result = mvc.post()
        .uri("/members/reset-password")
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
}
