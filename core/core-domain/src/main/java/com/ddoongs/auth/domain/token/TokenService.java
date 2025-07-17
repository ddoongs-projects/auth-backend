package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.member.InvalidTokenException;
import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberNotFoundException;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.shared.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final BlacklistTokenRepository blacklistTokenRepository;
  private final TokenProvider tokenProvider;
  private final MemberRepository memberRepository;

  @Transactional
  public TokenPair reissue(String refreshTokenValue) {
    String jti = tokenProvider.extractJti(refreshTokenValue);
    String email = tokenProvider.extractSubject(refreshTokenValue);

    validateNotBlacklisted(jti);

    blacklistOldToken(jti);

    Member member =
        memberRepository.findByEmail(new Email(email)).orElseThrow(MemberNotFoundException::new);

    return issueNewTokenPair(member);
  }

  private TokenPair issueNewTokenPair(Member member) {

    RefreshToken newRefreshToken = tokenProvider.createRefreshToken(member);
    String newAccessToken = tokenProvider.createAccessToken(member);

    refreshTokenRepository.save(newRefreshToken);

    return new TokenPair(newAccessToken, newRefreshToken);
  }

  private void validateNotBlacklisted(String jti) {
    if (blacklistTokenRepository.exists(jti)) {
      throw new InvalidTokenException();
    }
  }

  private void blacklistOldToken(String jti) {
    RefreshToken oldToken =
        refreshTokenRepository.find(jti).orElseThrow(InvalidTokenException::new);

    refreshTokenRepository.delete(jti);
    blacklistTokenRepository.save(jti, oldToken.remainingTtl());
  }
}
