package com.ddoongs.auth.storage.db.core.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpo, Long> {

  boolean existsByEmail(String address);

  Optional<MemberJpo> findByEmail(String email);
}
