package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationFinder;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final PasswordEncoder passwordEncoder;
  private final VerificationFinder verificationFinder;
  private final MemberRepository memberRepository;

  @Transactional
  public Member register(RegisterMember registerMember, UUID verificationId) {
    Verification verification = verificationFinder.find(verificationId);

    verification.ensureValidFor(new Email(registerMember.email()), VerificationPurpose.REGISTER);

    Member member = Member.register(registerMember, passwordEncoder);
    return memberRepository.save(member);
  }
}
