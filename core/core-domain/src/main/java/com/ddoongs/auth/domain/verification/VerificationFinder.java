package com.ddoongs.auth.domain.verification;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VerificationFinder {

  private final VerificationRepository verificationRepository;

  public Verification find(UUID verificationId) {
    return verificationRepository
        .find(verificationId)
        .orElseThrow(VerificationNotFoundException::new);
  }
}
