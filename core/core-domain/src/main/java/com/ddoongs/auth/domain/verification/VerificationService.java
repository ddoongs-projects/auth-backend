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

  @Transactional
  public Verification issue(CreateVerification createVerification) {
    requestIntervalValidator.validateInterval(createVerification);
    Verification verification = Verification.create(createVerification, verificationCodeGenerator);
    return verificationRepository.save(verification);
  }
}
