package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Transactional
@Import(AuthTestConfiguration.class)
@SpringBootTest(classes = TestApplication.class)
class VerificationServiceTest {

  @Autowired
  VerificationService verificationService;

  @Autowired
  VerificationRepository verificationRepository;

  @MockitoBean
  DateTimeProvider dateTimeProvider;

  @MockitoSpyBean
  AuditingHandler auditingHandler;

  @BeforeEach
  void setup() {
    given(dateTimeProvider.getNow()).willReturn(Optional.of(LocalDateTime.now()));
    auditingHandler.setDateTimeProvider(dateTimeProvider);
  }

  @DisplayName("인증을 성공적으로 발급한다.")
  @Test
  void issue_success() {
    CreateVerification createVerification = VerificationFixture.createVerification();
    Verification issue = verificationService.issue(createVerification);

    Verification foundVerification = verificationRepository
        .findLatest(createVerification.email(), createVerification.purpose())
        .orElseThrow();

    assertThat(foundVerification.getEmail()).isEqualTo(createVerification.email());
    assertThat(foundVerification.getPurpose()).isEqualTo(createVerification.purpose());
    assertThat(foundVerification.getStatus()).isEqualTo(VerificationStatus.PENDING);
    assertThat(foundVerification.getCode().code()).isEqualTo("123456");
    assertThat(foundVerification.getDefaultDateTime().createdAt()).isNotNull();
  }

  @DisplayName("쿨다운이 완료되지 않으면 인증을 발급할 수 없다.")
  @Test
  void issue_fail_cooldown() {
    CreateVerification createVerification = VerificationFixture.createVerification();
    verificationService.issue(createVerification);

    assertThatThrownBy(() -> verificationService.issue(createVerification))
        .isInstanceOf(VerificationCooldownException.class);
  }

  @DisplayName("마지막 인증 발급이 쿨다운이 끝나고 난 이후에는 발급이 성공한다.")
  @Test
  void issue_success_after_cooldown() {
    given(dateTimeProvider.getNow()).willReturn(Optional.of(LocalDateTime.now().minusMinutes(2)));
    CreateVerification createVerification = VerificationFixture.createVerification();
    verificationService.issue(createVerification);

    given(dateTimeProvider.getNow()).willReturn(Optional.of(LocalDateTime.now()));
    Verification verification = verificationService.issue(createVerification);

    assertThat(verification.getEmail()).isEqualTo(createVerification.email());
    assertThat(verification.getPurpose()).isEqualTo(createVerification.purpose());
    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.PENDING);
    assertThat(verification.getCode().code()).isEqualTo("123456");
    assertThat(verification.getDefaultDateTime().createdAt()).isNotNull();
  }
}
