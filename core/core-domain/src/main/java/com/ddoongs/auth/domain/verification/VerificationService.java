package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class VerificationService {

  private final VerificationRepository verificationRepository;
  private final RequestIntervalValidator requestIntervalValidator;
  private final VerificationCodeGenerator verificationCodeGenerator;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public Verification issue(CreateVerification createVerification) {
    requestIntervalValidator.validateInterval(createVerification);

    Verification verification = Verification.create(createVerification, verificationCodeGenerator);

    verification = verificationRepository.save(verification);

    eventPublisher.publishEvent(new VerificationCreatedEvent(verification));

    return verification;
  }

  @Transactional
  public Verification verify(UUID verificationId, VerificationCode code) {
    Verification verification =
        verificationRepository.find(verificationId).orElseThrow(NotFoundException::new);

    verification.verify(code);

    return verificationRepository.save(verification);
  }
}
