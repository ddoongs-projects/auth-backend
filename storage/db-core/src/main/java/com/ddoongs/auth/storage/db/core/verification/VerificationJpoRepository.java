package com.ddoongs.auth.storage.db.core.verification;

import com.ddoongs.auth.domain.verification.VerificationPurpose;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationJpoRepository extends JpaRepository<VerificationJpo, Long> {

  Optional<VerificationJpo> findById(UUID id);

  Optional<VerificationJpo> findFirstByEmailAndPurposeOrderByCreatedAtDesc(
      String email, VerificationPurpose purpose);
}
