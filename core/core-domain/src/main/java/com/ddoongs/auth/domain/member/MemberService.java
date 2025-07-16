package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.token.RefreshToken;
import com.ddoongs.auth.domain.token.RefreshTokenRepository;
import com.ddoongs.auth.domain.token.TokenPair;
import com.ddoongs.auth.domain.token.TokenProvider;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationFinder;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationRepository;
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
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

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
  public TokenPair login(LoginMember loginMember) {
    Member member = memberRepository
        .findByEmail(new Email(loginMember.email()))
        .orElseThrow(MemberNotFoundException::new);

    member.validatePassword(loginMember.password(), passwordEncoder);

    String accessToken = tokenProvider.createAccessToken(member);
    RefreshToken refreshToken = tokenProvider.createRefreshToken(member);

    refreshToken = refreshTokenRepository.save(refreshToken);

    return new TokenPair(accessToken, refreshToken);
  }
}
