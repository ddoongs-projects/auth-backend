package com.ddoongs.auth.domain.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class VerificationService {

  private final VerificationRepository verificationRepository;
  private final RequestIntervalValidator requestIntervalValidator;
  private final VerificationCodeGenerator verificationCodeGenerator;
  private final VerificationSender verificationSender;

  @Transactional
  public Verification issue(CreateVerification createVerification) {
    requestIntervalValidator.validateInterval(createVerification);

    Verification verification = Verification.create(createVerification, verificationCodeGenerator);

    verification = verificationRepository.save(verification);

    verificationSender.send(verification);

    return verification;
  }
}
