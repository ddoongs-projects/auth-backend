package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.Email;
import java.util.Optional;

public interface MemberRepository {

  Member save(Member member);

  Optional<Member> find(Long id);

  boolean existsByEmail(Email email);
}
