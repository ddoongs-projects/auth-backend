package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ddoongs.auth.TestApplication;
import com.ddoongs.auth.domain.AuthTestConfiguration;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Transactional
@Import(AuthTestConfiguration.class)
@SpringBootTest(classes = TestApplication.class)
class VerificationServiceTest {

  @Autowired
  VerificationService verificationService;

  @Autowired
  VerificationRepository verificationRepository;

  @DisplayName("인증을 성공적으로 발급한다.")
  @Test
  void issue_success() {
    CreateVerification createVerification = VerificationFixture.createVerification();
    verificationService.issue(createVerification);

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
}
