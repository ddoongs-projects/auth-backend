package com.ddoongs.auth.api.security;

import com.ddoongs.auth.domain.member.AppendProviderDetail;
import com.ddoongs.auth.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

  private final MemberService memberService;
  private final OidcUserService oidcUserService = new OidcUserService();

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    ClientRegistration clientRegistration = userRequest.getClientRegistration();
    String registrationId = clientRegistration.getRegistrationId();

    OidcUser oidcUser = oidcUserService.loadUser(userRequest);

    AppendProviderDetail providerUser = ProviderUserFactory.create(registrationId, oidcUser);

    memberService.registerOAuth2(providerUser);

    return oidcUser;
  }
}
