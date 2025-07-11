package com.ddoongs.auth.storage.db.core.verification;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class VerificationCoreRepository implements VerificationRepository {

  private final VerificationJpoRepository verificationJpoRepository;

  @Override
  public Verification save(final Verification verification) {
    return verificationJpoRepository
        .save(VerificationJpo.fromDomain(verification))
        .toDomain();
  }

  @Override
  public Optional<Verification> find(final UUID verificationId) {
    return verificationJpoRepository.findById(verificationId).map(VerificationJpo::toDomain);
  }

  @Override
  public Optional<Verification> findLatest(final Email email, final VerificationPurpose purpose) {
    return verificationJpoRepository
        .findFirstByEmailAndPurposeOrderByCreatedAtDesc(email.address(), purpose)
        .map(VerificationJpo::toDomain);
  }
}
