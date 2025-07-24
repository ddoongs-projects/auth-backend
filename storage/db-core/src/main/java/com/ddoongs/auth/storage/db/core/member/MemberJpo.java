package com.ddoongs.auth.storage.db.core.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.Password;
import com.ddoongs.auth.domain.member.ProviderDetail;
import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.storage.db.core.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@Entity
public class MemberJpo extends BaseEntity {

  @NaturalId
  private String email;

  private String password;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProviderDetailJpo> providerDetails = new ArrayList<>();

  public static MemberJpo fromDomain(Member member) {
    MemberJpo memberJpo = new MemberJpo();

    memberJpo.email = member.getEmail().address();

    memberJpo.password = member.getPassword().getPasswordHash();

    memberJpo.providerDetails = member.getProviderDetails().stream()
        .map(providerDetail -> ProviderDetailJpo.fromDomain(providerDetail, memberJpo))
        .collect(Collectors.toList());

    return memberJpo;
  }

  public Member toDomain() {
    List<ProviderDetail> providerDetails =
        this.providerDetails.stream().map(ProviderDetailJpo::toDomain).collect(Collectors.toList());
    return new Member(
        this.getId(),
        new Email(this.getEmail()),
        new Password(this.getPassword()),
        providerDetails,
        new DefaultDateTime(this.getCreatedAt(), this.getUpdatedAt()));
  }

  public void updateFromDomain(Member member) {
    this.password = member.getPassword().getPasswordHash();

    Map<Long, ProviderDetailJpo> existingProviderDetailsMap = this.providerDetails.stream()
        .collect(
            Collectors.toMap(ProviderDetailJpo::getId, providerDetailJpo -> providerDetailJpo));

    for (ProviderDetail providerDetail : member.getProviderDetails()) {
      if (providerDetail.getId() != null) {
        ProviderDetailJpo existingProviderDetail =
            existingProviderDetailsMap.get(providerDetail.getId());
        existingProviderDetailsMap.remove(providerDetail.getId());
        existingProviderDetail.updateFromDomain(providerDetail, this);
      } else {
        this.providerDetails.add(ProviderDetailJpo.fromDomain(providerDetail, this));
      }
    }

    for (ProviderDetailJpo providerDetailJpo : existingProviderDetailsMap.values()) {
      this.providerDetails.remove(providerDetailJpo);
    }
  }
}
