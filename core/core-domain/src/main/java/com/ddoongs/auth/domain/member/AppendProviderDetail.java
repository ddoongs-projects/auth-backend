package com.ddoongs.auth.domain.member;

public record AppendProviderDetail(Provider provider, String providerId, String email) {

  public ProviderDetail toProviderDetail() {
    return ProviderDetail.of(this.provider, this.providerId);
  }
}
