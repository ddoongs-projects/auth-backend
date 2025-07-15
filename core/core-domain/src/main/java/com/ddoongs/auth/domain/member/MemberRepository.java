package com.ddoongs.auth.domain.member;

import java.util.Optional;

public interface MemberRepository {

  Member save(Member member);

  Optional<Member> find(Long id);
}
