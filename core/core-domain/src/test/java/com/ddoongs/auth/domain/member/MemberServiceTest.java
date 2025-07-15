package com.ddoongs.auth.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.support.FakeVerificationCodeGenerator;
import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationMismatchException;
import com.ddoongs.auth.domain.verification.VerificationNotCompletedException;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Import(AuthTestConfiguration.class)
@SpringBootTest(classes = TestApplication.class)
class MemberServiceTest {

  @Autowired
  private VerificationService verificationService;

  @Autowired
  private FakeVerificationCodeGenerator verificationCodeGenerator;

  @Autowired
  private MemberService memberService;

  @BeforeEach
  void setup() {
    verificationCodeGenerator.setFixedCode("123456");
  }

  @DisplayName("회원 등록을 할 수 있다.")
  @Test
  void register() {
    String email = "test@test.com";
    Verification verification = prepareRegister(email);

    Member member =
        memberService.register(MemberFixture.registerMember(email), verification.getId());

    assertThat(member.getId()).isNotNull();
    assertThat(member.getEmail().address()).isEqualTo(email);
    assertThat(member.getDefaultDateTime()).isNotNull();
  }

  @DisplayName("인증이 완료되지 않은 경우 회원 등록에 실패한다.")
  @Test
  void registerFailNotVerified() {
    String email = "test@test.com";
    CreateVerification createVerification =
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER);
    Verification verification = verificationService.issue(createVerification);

    assertThatThrownBy(
            () -> memberService.register(MemberFixture.registerMember(email), verification.getId()))
        .isInstanceOf(VerificationNotCompletedException.class);
  }

  @DisplayName("인증 목적이 일치하지 않는 경우 회원 등록에 실패한다.")
  @Test
  void registerFailDifferentPurpose() {
    String email = "test@test.com";
    CreateVerification createVerification =
        new CreateVerification(new Email(email), VerificationPurpose.RESET_PASSWORD);
    Verification verification = verificationService.issue(createVerification);

    assertThatThrownBy(
            () -> memberService.register(MemberFixture.registerMember(email), verification.getId()))
        .isInstanceOf(VerificationMismatchException.class);
  }

  @DisplayName("인증 이메일과 일치하지 않는 경우 회원 등록에 실패한다.")
  @Test
  void registerFailDifferentEmail() {
    String email = "test@test.com";
    CreateVerification createVerification =
        new CreateVerification(new Email("different@test.com"), VerificationPurpose.RESET_PASSWORD);
    Verification verification = verificationService.issue(createVerification);

    assertThatThrownBy(
            () -> memberService.register(MemberFixture.registerMember(email), verification.getId()))
        .isInstanceOf(VerificationMismatchException.class);
  }

  private Verification prepareRegister(String email) {
    CreateVerification createVerification =
        new CreateVerification(new Email(email), VerificationPurpose.REGISTER);
    Verification verification = verificationService.issue(createVerification);

    verificationService.verify(verification.getId(), new VerificationCode("123456"));
    return verification;
  }
}
