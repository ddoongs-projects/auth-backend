package com.ddoongs.auth.domain.verification;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.support.TestFixture;
import com.ddoongs.auth.domain.support.VerificationFixture;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestIntervalValidatorTest {

  private final long INTERVAL_SECONDS = 60;

  @Mock
  private VerificationRepository verificationRepository;

  @InjectMocks
  private RequestIntervalValidator requestIntervalValidator;

  private VerificationCodeGenerator verificationCodeGenerator;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    verificationCodeGenerator = TestFixture.verificationCodeGenerator("123456");
    Field intervalSecondsField = RequestIntervalValidator.class.getDeclaredField("intervalSeconds");
    intervalSecondsField.setAccessible(true);
    intervalSecondsField.set(requestIntervalValidator, INTERVAL_SECONDS);
  }

  @DisplayName("이전 인증 요청이 없는 경우 예외가 발생하지 않는다.")
  @Test
  void validateInterval_noPreviousVerification_noExceptionThrown() {
    CreateVerification createVerification =
        VerificationFixture.createVerification(VerificationPurpose.REGISTER);
    when(verificationRepository.findLatest(
            createVerification.email(), createVerification.purpose()))
        .thenReturn(Optional.empty());

    assertThatCode(() -> requestIntervalValidator.validateInterval(createVerification))
        .doesNotThrowAnyException();
  }

  @DisplayName("이전 인증 요청이 쿨다운 시간 내에 있는 경우 예외가 발생한다.")
  @Test
  void validateInterval_previousVerificationWithinCooldown_throwsVerificationCooldownException() {
    CreateVerification createVerification = VerificationFixture.createVerification();
    LocalDateTime createdAt = LocalDateTime.now().minusSeconds(INTERVAL_SECONDS / 2);
    Verification lastVerification = VerificationFixture.withDefaultDateTime(
        createVerification, verificationCodeGenerator, new DefaultDateTime(createdAt, createdAt));

    when(verificationRepository.findLatest(
            createVerification.email(), createVerification.purpose()))
        .thenReturn(Optional.of(lastVerification));

    assertThatThrownBy(() -> requestIntervalValidator.validateInterval(createVerification))
        .isInstanceOf(VerificationCooldownException.class);
  }

  @DisplayName("이전 인증 요청이 쿨다운 시간을 지난 경우 예외가 발생하지 않는다.")
  @Test
  void validateInterval_previousVerificationOutsideCooldown_noExceptionThrown() {
    CreateVerification createVerification = VerificationFixture.createVerification();
    LocalDateTime createdAt = LocalDateTime.now().minusSeconds(INTERVAL_SECONDS + 1);
    Verification lastVerification = VerificationFixture.withDefaultDateTime(
        createVerification, verificationCodeGenerator, new DefaultDateTime(createdAt, createdAt));

    when(verificationRepository.findLatest(
            createVerification.email(), createVerification.purpose()))
        .thenReturn(Optional.of(lastVerification));

    assertThatCode(() -> requestIntervalValidator.validateInterval(createVerification))
        .doesNotThrowAnyException();
  }
}
