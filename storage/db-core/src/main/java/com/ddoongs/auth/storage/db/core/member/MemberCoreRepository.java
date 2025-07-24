package com.ddoongs.auth.storage.db.core.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.Provider;
import com.ddoongs.auth.domain.shared.Email;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberCoreRepository implements MemberRepository {

  private final MemberJpaRepository memberJpaRepository;

  @Override
  public Member save(Member member) {
    return Optional.ofNullable(member.getId())
        .flatMap(memberJpaRepository::findByIdWithProviderDetails)
        .map(jpo -> {
          jpo.updateFromDomain(member);
          return jpo.toDomain();
        })
        .orElseGet(() -> memberJpaRepository.save(MemberJpo.fromDomain(member)).toDomain());
  }

  @Override
  public Optional<Member> find(Long id) {
    return memberJpaRepository.findByIdWithProviderDetails(id).map(MemberJpo::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return memberJpaRepository.existsByEmail(email.address());
  }

  @Override
  public Optional<Member> findByEmail(Email email) {
    return memberJpaRepository
        .findByEmailWithProviderDetails(email.address())
        .map(MemberJpo::toDomain);
  }

  @Override
  public Optional<Member> findOAuth2(Provider provider, String providerId) {
    return memberJpaRepository
        .findByProviderAndProviderIdWithProviderDetails(provider, providerId)
        .map(MemberJpo::toDomain);
  }
}
