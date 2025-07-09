package com.ddoongs.auth.domain.verification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RequestIntervalValidator {

  private final VerificationRepository verificationRepository;

  @Value("${verification.request.interval-seconds:60}")
  private long intervalSeconds;

  public void validateInterval(CreateVerification createVerification) {
    Optional<Verification> optionalLastVerification =
        verificationRepository.findLatest(createVerification.email(), createVerification.purpose());

    if (optionalLastVerification.isEmpty()) {
      return;
    }

    Verification lastVerification = optionalLastVerification.get();
    final LocalDateTime now = LocalDateTime.now();
    final Duration requestInterval = Duration.ofSeconds(intervalSeconds);
    final Duration remainCoolDown = Duration.between(
        now, lastVerification.getDefaultDateTime().createdAt().plus(requestInterval));

    if (remainCoolDown.isPositive()) {
      throw new VerificationCooldownException(remainCoolDown);
    }
  }
}
