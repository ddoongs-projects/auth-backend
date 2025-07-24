package com.ddoongs.auth.domain.member;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class ProviderDetail {

  private Long id;
  private Provider provider;
  private String providerId;
  private DefaultDateTime defaultDateTime;

  public static ProviderDetail of(Provider provider, String providerId) {
    ProviderDetail providerDetail = new ProviderDetail();
    providerDetail.provider = provider;
    providerDetail.providerId = providerId;
    return providerDetail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProviderDetail that = (ProviderDetail) o;
    return provider == that.provider && Objects.equals(providerId, that.providerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, providerId);
  }
}
