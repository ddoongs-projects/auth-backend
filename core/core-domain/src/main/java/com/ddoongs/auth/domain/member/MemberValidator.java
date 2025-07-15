package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberValidator {

  private final MemberRepository memberRepository;

  public void validateEmailUnique(Email email) {
    if (memberRepository.existsByEmail(email)) {
      throw new DuplicatedEmailException(email);
    }
  }
}
