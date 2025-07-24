package com.ddoongs.auth.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import com.ddoongs.auth.domain.ServiceTestSupport;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.support.FakeVerificationCodeGenerator;
import com.ddoongs.auth.domain.support.MemberFixture;
import com.ddoongs.auth.domain.verification.CreateVerification;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationFinder;
import com.ddoongs.auth.domain.verification.VerificationMismatchException;
import com.ddoongs.auth.domain.verification.VerificationNotCompletedException;
import com.ddoongs.auth.domain.verification.VerificationNotFoundException;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationService;
import com.ddoongs.auth.domain.verification.VerificationStatus;
import java.util.ArrayList;
import java.util.UUID;
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

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private VerificationFinder verificationFinder;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setup() {
    verificationCodeGenerator.setFixedCode("123456");
  }

  @DisplayName("회원 등록을 할 수 있다.")
  @Test
  void register() {
    String email = "test@test.com";
    Verification verification = ServiceTestSupport.prepareRegister(verificationService, email);

    Member member =
        memberService.register(MemberFixture.registerMember(email), verification.getId());

    assertThat(member.getId()).isNotNull();
    assertThat(member.getEmail().address()).isEqualTo(email);
    assertThat(member.getDefaultDateTime()).isNotNull();
    assertThat(member.getProviderDetails()).isEmpty();

    Verification verification1 = verificationFinder.find(verification.getId());

    assertThat(verification1.getStatus()).isEqualTo(VerificationStatus.CONSUMED);
  }

  @DisplayName("중복된 이메일로 등록 시 회원 등록에 실패한다.")
  @Test
  void registerFailDuplicatedEmail() {
    String email = "test@test.com";

    memberRepository.save(
        new Member(null, new Email(email), new Password("123123123"), new ArrayList<>(), null));

    Verification verification2 = ServiceTestSupport.prepareRegister(verificationService, email);

    assertThatThrownBy(() ->
            memberService.register(MemberFixture.registerMember(email), verification2.getId()))
        .isInstanceOf(DuplicatedEmailException.class);

    Verification foundVerification = verificationFinder.find(verification2.getId());

    assertThat(foundVerification.getStatus()).isNotEqualTo(VerificationStatus.CONSUMED);
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

    Verification foundVerification = verificationFinder.find(verification.getId());

    assertThat(foundVerification.getStatus()).isNotEqualTo(VerificationStatus.CONSUMED);
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

    Verification foundVerification = verificationFinder.find(verification.getId());

    assertThat(foundVerification.getStatus()).isNotEqualTo(VerificationStatus.CONSUMED);
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

    Verification foundVerification = verificationFinder.find(verification.getId());

    assertThat(foundVerification.getStatus()).isNotEqualTo(VerificationStatus.CONSUMED);
  }

  @DisplayName("존재하지 않는 회원에 대해서 비밀번호 초기화가 실패한다.")
  @Test
  void resetPasswordNotFoundMember() {
    Email email = new Email("test@test.com");
    String password = "123qwe!@#";
    UUID verificationId = UUID.randomUUID();

    assertThatThrownBy(() -> memberService.resetPassword(email, password, verificationId))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @DisplayName("존재하지 않는 인증에 대해서 비밀번호 초기화가 실패한다.")
  @Test
  void resetPasswordNotFoundVerification() {
    Email email = new Email("test@test.com");
    String password = "123qwe!@#";

    Member member = memberRepository.save(
        new Member(null, email, Password.of(password, passwordEncoder), new ArrayList<>(), null));

    UUID verificationId = UUID.randomUUID();

    assertThatThrownBy(() -> memberService.resetPassword(email, password, verificationId))
        .isInstanceOf(VerificationNotFoundException.class);
  }

  @DisplayName("인증완료되지 않은 인증에 대해서 비밀번호 초기화가 실패한다.")
  @Test
  void resetPasswordNotVerifiedVerification() {
    Email email = new Email("test@test.com");
    String password = "123qwe!@#";

    Member member = memberRepository.save(
        new Member(null, email, Password.of(password, passwordEncoder), new ArrayList<>(), null));

    Verification verification = verificationService.issue(
        new CreateVerification(email, VerificationPurpose.RESET_PASSWORD));

    assertThatThrownBy(() -> memberService.resetPassword(email, password, verification.getId()))
        .isInstanceOf(VerificationNotCompletedException.class);
  }

  @DisplayName("인증 목적이 다른 인증에 대해서 비밀번호 초기화가 실패한다.")
  @Test
  void resetPasswordDifferentPurposeVerification() {
    Email email = new Email("test@test.com");
    String password = "123qwe!@#";

    Member member =
        new Member(null, email, Password.of(password, passwordEncoder), new ArrayList<>(), null);
    member = memberRepository.save(member);

    Verification verification =
        verificationService.issue(new CreateVerification(email, VerificationPurpose.REGISTER));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    assertThatThrownBy(() -> memberService.resetPassword(email, password, verification.getId()))
        .isInstanceOf(VerificationMismatchException.class);
  }

  @DisplayName("다른 이메일의 인증에 대해서 비밀번호 초기화가 실패한다.")
  @Test
  void resetPasswordDifferentEmailVerification() {
    Email email = new Email("test@test.com");
    String password = "123qwe!@#";

    Member member =
        new Member(null, email, Password.of(password, passwordEncoder), new ArrayList<>(), null);
    member = memberRepository.save(member);

    Verification verification = verificationService.issue(
        new CreateVerification(new Email("different@test.com"), VerificationPurpose.REGISTER));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    assertThatThrownBy(() -> memberService.resetPassword(email, password, verification.getId()))
        .isInstanceOf(VerificationMismatchException.class);
  }

  @DisplayName("비밀번호를 초기화한다.")
  @Test
  void resetPassword() {
    Email email = new Email("test@test.com");
    String password = "123qwe!@#";

    Member member =
        new Member(null, email, Password.of(password, passwordEncoder), new ArrayList<>(), null);
    member = memberRepository.save(member);

    Verification verification = verificationService.issue(
        new CreateVerification(email, VerificationPurpose.RESET_PASSWORD));

    verificationService.verify(verification.getId(), new VerificationCode("123456"));

    String newPassword = "456rty$%^";

    assertThatCode(() -> memberService.resetPassword(email, newPassword, verification.getId()))
        .doesNotThrowAnyException();

    Member foundMember = memberRepository.find(member.getId()).orElseThrow();

    assertThatCode(() -> foundMember.validatePassword(newPassword, passwordEncoder))
        .doesNotThrowAnyException();
  }

  @DisplayName("OAuth2로 새로운 회원을 등록한다.")
  @Test
  void registerOAuth2_newMember() {
    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "oauth@test.com");

    Member registeredMember = memberService.registerOAuth2(providerDetail);

    assertThat(registeredMember.getId()).isNotNull();
    assertThat(registeredMember.getEmail().address()).isEqualTo("oauth@test.com");
    assertThat(registeredMember.getProviderDetails()).hasSize(1);
    assertThat(registeredMember.getProviderDetails().get(0).getProvider())
        .isEqualTo(Provider.GOOGLE);
    assertThat(registeredMember.getProviderDetails().get(0).getProviderId()).isEqualTo("google123");
  }

  @DisplayName("이미 등록된 OAuth2 회원이면 기존 회원을 반환한다.")
  @Test
  void registerOAuth2_existingOAuth2Member() {
    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", "oauth@test.com");

    Member firstMember = memberService.registerOAuth2(providerDetail);
    Member secondMember = memberService.registerOAuth2(providerDetail);

    assertThat(firstMember.getId()).isEqualTo(secondMember.getId());
    assertThat(secondMember.getProviderDetails()).hasSize(1);
  }

  @DisplayName("동일한 이메일을 가진 기존 회원에 OAuth2 정보를 연결한다.")
  @Test
  void registerOAuth2_connectToExistingEmailMember() {
    String email = "existing@test.com";
    Member existingMember = memberRepository.save(new Member(
        null,
        new Email(email),
        Password.of("password123", passwordEncoder),
        new ArrayList<>(),
        null));

    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", email);

    Member connectedMember = memberService.registerOAuth2(providerDetail);

    assertThat(connectedMember.getId()).isEqualTo(existingMember.getId());
    assertThat(connectedMember.getProviderDetails()).hasSize(1);
    assertThat(connectedMember.getProviderDetails().get(0).getProvider())
        .isEqualTo(Provider.GOOGLE);
    assertThat(connectedMember.getProviderDetails().get(0).getProviderId()).isEqualTo("google123");
  }

  @DisplayName("동일한 이메일 회원에 여러 OAuth2 제공자를 연결할 수 있다.")
  @Test
  void registerOAuth2_multipleProvidersToSameMember() {
    String email = "multi@test.com";
    Member existingMember = memberRepository.save(new Member(
        null,
        new Email(email),
        Password.of("password123", passwordEncoder),
        new ArrayList<>(),
        null));

    AppendProviderDetail googleDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", email);
    AppendProviderDetail kakaoDetail = new AppendProviderDetail(Provider.KAKAO, "kakao456", email);

    Member googleConnected = memberService.registerOAuth2(googleDetail);
    Member kakaoConnected = memberService.registerOAuth2(kakaoDetail);

    assertThat(googleConnected.getId()).isEqualTo(existingMember.getId());
    assertThat(kakaoConnected.getId()).isEqualTo(existingMember.getId());
    assertThat(kakaoConnected.getProviderDetails()).hasSize(2);
    assertThat(kakaoConnected.getProviderDetails())
        .extracting(ProviderDetail::getProvider)
        .containsExactlyInAnyOrder(Provider.GOOGLE, Provider.KAKAO);
  }

  @DisplayName("이미 연결된 OAuth2 정보로 다시 등록하면 기존 회원을 반환한다.")
  @Test
  void registerOAuth2_alreadyConnectedProvider() {
    String email = "connected@test.com";
    Member existingMember = memberRepository.save(new Member(
        null,
        new Email(email),
        Password.of("password123", passwordEncoder),
        new ArrayList<>(),
        null));

    AppendProviderDetail providerDetail =
        new AppendProviderDetail(Provider.GOOGLE, "google123", email);

    Member firstConnect = memberService.registerOAuth2(providerDetail);
    Member secondConnect = memberService.registerOAuth2(providerDetail);

    assertThat(firstConnect.getId()).isEqualTo(existingMember.getId());
    assertThat(secondConnect.getId()).isEqualTo(existingMember.getId());
    assertThat(secondConnect.getProviderDetails()).hasSize(1);
  }
}
