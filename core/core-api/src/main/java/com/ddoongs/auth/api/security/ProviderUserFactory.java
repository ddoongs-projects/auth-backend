package com.ddoongs.auth.api.security;

import com.ddoongs.auth.domain.member.AppendProviderDetail;
import com.ddoongs.auth.domain.member.Provider;
import java.util.Map;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class ProviderUserFactory {

  private ProviderUserFactory() {}

  public static AppendProviderDetail create(String registrationId, OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();

    return switch (registrationId) {
      case ("google") -> ProviderUserFactory.ofGoogle(attributes);
      case ("naver") -> ProviderUserFactory.ofNaver(attributes);
      case ("kakao") -> ProviderUserFactory.ofKakao(attributes);
      default -> throw new IllegalStateException("Unexpected value: " + registrationId);
    };
  }

  private static AppendProviderDetail ofNaver(Map<String, Object> attributes) {
    attributes = (Map<String, Object>) attributes.get("response");

    return new AppendProviderDetail(
        Provider.NAVER, (String) attributes.get("id"), (String) attributes.get("email"));
  }

  private static AppendProviderDetail ofGoogle(Map<String, Object> attributes) {
    return new AppendProviderDetail(
        Provider.GOOGLE, (String) attributes.get("sub"), (String) attributes.get("email"));
  }

  private static AppendProviderDetail ofKakao(Map<String, Object> attributes) {
    return new AppendProviderDetail(
        Provider.KAKAO, (String) attributes.get("sub"), (String) attributes.get("email"));
  }
}
