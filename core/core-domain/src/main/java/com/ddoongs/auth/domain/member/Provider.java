package com.ddoongs.auth.domain.member;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum Provider {
  GOOGLE("google"),
  KAKAO("kakao"),
  NAVER("naver");

  private static final Map<String, Provider> VALUE_MAP = Stream.of(values())
      .collect(Collectors.toMap(Provider::getRegistrationId, Function.identity()));
  private final String registrationId;

  Provider(String registrationId) {
    this.registrationId = registrationId;
  }

  public static Provider fromRegistrationId(String registrationId) {
    Provider provider = VALUE_MAP.get(registrationId.toLowerCase());
    if (provider == null) {
      throw new IllegalArgumentException("'" + registrationId + "'에 해당하는 Provider가 없습니다.");
    }
    return provider;
  }
}
