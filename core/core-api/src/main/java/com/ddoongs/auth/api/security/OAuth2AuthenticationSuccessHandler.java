package com.ddoongs.auth.api.security;

import com.ddoongs.auth.domain.member.AppendProviderDetail;
import com.ddoongs.auth.domain.member.Provider;
import com.ddoongs.auth.domain.token.PrepareTokenExchange;
import com.ddoongs.auth.domain.token.TokenExchange;
import com.ddoongs.auth.domain.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final String frontendRedirectUrl;
  private final TokenService tokenService;

  public OAuth2AuthenticationSuccessHandler(
      @Value("${frontend.base-url}") String frontendBaseUrl,
      @Value("${frontend.auth-code-redirect-uri}") String authCodeRedirectUri,
      TokenService tokenService) {
    this.frontendRedirectUrl = frontendBaseUrl + authCodeRedirectUri;
    this.tokenService = tokenService;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2AuthenticationToken oAuth2AuthenticationToken =
        (OAuth2AuthenticationToken) authentication;
    String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
    OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();

    AppendProviderDetail providerUser = ProviderUserFactory.create(registrationId, oAuth2User);
    Provider provider = Provider.fromRegistrationId(registrationId);

    TokenExchange tokenExchange = tokenService.prepareTokenExchange(
        new PrepareTokenExchange(provider, providerUser.providerId()));
    String redirectUri = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
        .queryParam("authCode", tokenExchange.authCode())
        .build()
        .encode()
        .toUriString();

    super.getRedirectStrategy().sendRedirect(request, response, redirectUri);
  }
}
