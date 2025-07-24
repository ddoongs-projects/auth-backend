package com.ddoongs.auth.storage.db.core.member;

import com.ddoongs.auth.domain.member.Provider;
import com.ddoongs.auth.domain.member.ProviderDetail;
import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.storage.db.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "provider_detail")
@Entity
public class ProviderDetailJpo extends BaseEntity {

  @Enumerated(EnumType.STRING)
  private Provider provider;

  private String providerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private MemberJpo member;

  public static ProviderDetailJpo fromDomain(ProviderDetail providerDetail, MemberJpo memberJpo) {
    ProviderDetailJpo providerDetailJpo = new ProviderDetailJpo();
    providerDetailJpo.provider = providerDetail.getProvider();
    providerDetailJpo.providerId = providerDetail.getProviderId();
    providerDetailJpo.member = memberJpo;
    return providerDetailJpo;
  }

  public ProviderDetail toDomain() {
    return new ProviderDetail(
        this.getId(),
        this.provider,
        this.providerId,
        new DefaultDateTime(this.getCreatedAt(), this.getUpdatedAt()));
  }

  public void updateFromDomain(ProviderDetail providerDetail, MemberJpo member) {
    this.provider = providerDetail.getProvider();
    this.providerId = providerDetail.getProviderId();
    this.member = member;
  }
}
