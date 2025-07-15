package com.ddoongs.auth.storage.db.core.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberCoreRepository implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;

  @Override
  public Member save(Member member) {
    return memberJpaRepository.save(MemberJpo.fromDomain(member)).toDomain();
  }

  @Override
  public Optional<Member> find(Long id) {
    return memberJpaRepository.findById(id).map(MemberJpo::toDomain);
  }
}
