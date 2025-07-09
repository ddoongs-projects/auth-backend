package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.Email;
import java.util.Optional;
import java.util.UUID;

public interface VerificationRepository {

  Verification save(Verification verification);

  Optional<Verification> findById(UUID verificationId);

  Optional<Verification> findLatest(Email email, VerificationPurpose purpose);
}
