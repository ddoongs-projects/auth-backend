package com.ddoongs.auth.api.security;

import com.ddoongs.auth.domain.member.AppendProviderDetail;
import com.ddoongs.auth.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final MemberService memberService;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService =
      new DefaultOAuth2UserService();

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    ClientRegistration clientRegistration = userRequest.getClientRegistration();
    String registrationId = clientRegistration.getRegistrationId();

    OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

    AppendProviderDetail providerUser = ProviderUserFactory.create(registrationId, oAuth2User);

    memberService.registerOAuth2(providerUser);

    return oAuth2User;
  }
}
