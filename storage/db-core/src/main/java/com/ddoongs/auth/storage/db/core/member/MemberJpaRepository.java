package com.ddoongs.auth.storage.db.core.member;

import com.ddoongs.auth.domain.member.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberJpaRepository extends JpaRepository<MemberJpo, Long> {

  @Query("SELECT DISTINCT m FROM MemberJpo m LEFT JOIN FETCH m.providerDetails WHERE m.id = :id")
  Optional<MemberJpo> findByIdWithProviderDetails(Long id);

  boolean existsByEmail(String email);

  @Query(
      "SELECT DISTINCT m FROM MemberJpo m LEFT JOIN FETCH m.providerDetails WHERE m.email = :email")
  Optional<MemberJpo> findByEmailWithProviderDetails(String email);

  @Query(
      "SELECT DISTINCT m FROM MemberJpo m JOIN FETCH m.providerDetails t WHERE t.provider = :provider AND t.providerId = :providerId")
  Optional<MemberJpo> findByProviderAndProviderIdWithProviderDetails(
      Provider provider, String providerId);
}
