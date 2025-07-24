package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.MemberNotFoundException;
import com.ddoongs.auth.domain.member.MemberRepository;
import com.ddoongs.auth.domain.member.PasswordEncoder;
import com.ddoongs.auth.domain.shared.Email;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenService {

  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final BlacklistTokenRepository blacklistTokenRepository;
  private final TokenProvider tokenProvider;
  private final MemberRepository memberRepository;
  private final TokenIssuer tokenIssuer;
  private final Clock clock;
  private final TokenExchangeRepository tokenExchangeRepository;

  @Transactional
  public TokenPair login(LoginMember loginMember) {
    Member member = memberRepository
        .findByEmail(new Email(loginMember.email()))
        .orElseThrow(MemberNotFoundException::new);

    member.validatePassword(loginMember.password(), passwordEncoder);

    String accessToken = tokenIssuer.issueAccessToken(member);
    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);

    refreshToken = refreshTokenRepository.save(refreshToken);

    return new TokenPair(accessToken, refreshToken);
  }

  @Transactional
  public TokenPair reissue(String refreshTokenValue) {
    tokenProvider.validate(refreshTokenValue);

    String jti = tokenProvider.extractJti(refreshTokenValue);
    Long memberId = Long.parseLong(tokenProvider.extractSubject(refreshTokenValue));

    validateNotBlacklisted(jti);

    blacklistOldToken(jti);

    Member member = memberRepository.find(memberId).orElseThrow(MemberNotFoundException::new);

    return issueNewTokenPair(member);
  }

  @Transactional
  public void logout(LogoutMember logoutMember) {
    String accessToken = logoutMember.accessToken();
    String refreshToken = logoutMember.refreshToken();

    blacklistTokenRepository.save(
        tokenProvider.extractJti(accessToken), tokenProvider.getRemainingAccessTtl(accessToken));

    blacklistTokenRepository.save(
        tokenProvider.extractJti(refreshToken), tokenProvider.getRemainingAccessTtl(refreshToken));
  }

  private TokenPair issueNewTokenPair(Member member) {
    RefreshToken newRefreshToken = tokenIssuer.issueRefreshToken(member);
    String newAccessToken = tokenIssuer.issueAccessToken(member);

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
    blacklistTokenRepository.save(jti, oldToken.remainingTtl(clock));
  }

  @Transactional
  public TokenExchange prepareTokenExchange(PrepareTokenExchange prepareTokenExchange) {
    Member member = memberRepository
        .findOAuth2(prepareTokenExchange.provider(), prepareTokenExchange.providerId())
        .orElseThrow(MemberNotFoundException::new);

    UUID authCode = UUID.randomUUID();

    String accessToken = tokenIssuer.issueAccessToken(member);
    RefreshToken refreshToken = tokenIssuer.issueRefreshToken(member);
    TokenPair tokenPair = new TokenPair(accessToken, refreshToken);

    TokenExchange tokenExchange = new TokenExchange(authCode, tokenPair);

    return tokenExchangeRepository.save(tokenExchange);
  }

  @Transactional
  public TokenPair exchangeToken(UUID authCode) {
    TokenExchange tokenExchange =
        tokenExchangeRepository.find(authCode).orElseThrow(InvalidAuthCodeException::new);
    tokenExchangeRepository.delete(authCode);
    return tokenExchange.tokenPair();
  }
}
