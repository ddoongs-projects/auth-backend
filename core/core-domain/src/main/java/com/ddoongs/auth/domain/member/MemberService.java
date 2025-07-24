package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationFinder;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationRepository;
import java.util.Optional;
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
  private final MemberValidator memberValidator;
  private final VerificationRepository verificationRepository;

  @Transactional
  public Member register(RegisterMember registerMember, UUID verificationId) {
    memberValidator.validateEmailUnique(new Email(registerMember.email()));

    Verification verification = verificationFinder.find(verificationId);

    verification.ensureValidFor(new Email(registerMember.email()), VerificationPurpose.REGISTER);

    Member member = Member.register(registerMember, passwordEncoder);

    verification.consume();

    verificationRepository.save(verification);

    return memberRepository.save(member);
  }

  @Transactional
  public Member resetPassword(Email email, String password, UUID verificationId) {
    Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

    Verification verification = verificationFinder.find(verificationId);

    verification.ensureValidFor(email, VerificationPurpose.RESET_PASSWORD);
    verification.consume();

    member.changePassword(password, passwordEncoder);

    verificationRepository.save(verification);
    return memberRepository.save(member);
  }

  @Transactional
  public Member registerOAuth2(AppendProviderDetail appendProviderDetail) {
    return memberRepository
        .findOAuth2(appendProviderDetail.provider(), appendProviderDetail.providerId())
        .or(() -> connectWithSameEmailMember(appendProviderDetail))
        .orElseGet(() -> registerNewOAuth2Member(appendProviderDetail));
  }

  private Optional<Member> connectWithSameEmailMember(AppendProviderDetail providerUser) {
    return memberRepository.findByEmail(new Email(providerUser.email())).map(existingMember -> {
      existingMember.connectOAuth2(providerUser);
      return memberRepository.save(existingMember);
    });
  }

  private Member registerNewOAuth2Member(AppendProviderDetail providerUser) {
    Member newMember = Member.registerOAuth2(providerUser, passwordEncoder);
    return memberRepository.save(newMember);
  }

  public Member find(Long id) {
    return memberRepository.find(id).orElseThrow(MemberNotFoundException::new);
  }
}
