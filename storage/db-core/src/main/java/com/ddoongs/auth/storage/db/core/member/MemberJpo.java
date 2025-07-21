package com.ddoongs.auth.storage.db.core.member;

import com.ddoongs.auth.domain.member.Member;
import com.ddoongs.auth.domain.member.Password;
import com.ddoongs.auth.domain.member.Provider;
import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.storage.db.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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

  @Enumerated(EnumType.STRING)
  private Provider provider;

  private String providerId;

  public static MemberJpo fromDomain(Member member) {
    return new MemberJpo(
        member.getEmail().address(),
        member.getPassword().getPasswordHash(),
        member.getProvider(),
        member.getProviderId());
  }

  public Member toDomain() {
    return new Member(
        this.getId(),
        new Email(this.getEmail()),
        new Password(this.getPassword()),
        this.provider,
        this.providerId,
        new DefaultDateTime(this.getCreatedAt(), this.getUpdatedAt()));
  }

  public void updateFromDomain(Member member) {
    this.password = member.getPassword().getPasswordHash();
  }
}
