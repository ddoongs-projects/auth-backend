package com.ddoongs.auth.api.auth;

import com.ddoongs.auth.domain.token.TokenPair;
import com.ddoongs.auth.domain.token.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthApi {

  private final TokenService tokenService;

  @PostMapping("/auth/login")
  public TokenResponse login(@RequestBody @Valid MemberLoginRequest request) {
    TokenPair tokenPair = tokenService.login(request.toLoginMember());
    return TokenResponse.of(tokenPair);
  }

  @PostMapping("/auth/reissue")
  public TokenResponse reissue(@RequestBody @Valid ReissueRequest request) {
    TokenPair tokenPair = tokenService.reissue(request.refreshToken());
    return TokenResponse.of(tokenPair);
  }

  @PostMapping("/auth/renew")
  public TokenResponse renew(@RequestBody @Valid RenewRequest request) {
    TokenPair tokenPair = tokenService.renew(request.refreshToken());
    return TokenResponse.of(tokenPair);
  }

  @PostMapping("/auth/logout")
  public void logout(@RequestBody @Valid MemberLogoutRequest request) {
    tokenService.logout(request.toLogoutMember());
  }
}
