package com.ddoongs.auth.storage.db.core.verification;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import com.ddoongs.auth.domain.verification.Verification;
import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationPurpose;
import com.ddoongs.auth.domain.verification.VerificationStatus;
import com.ddoongs.auth.storage.db.core.MetaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "verification")
@Entity
public class VerificationJpo extends MetaEntity {

  @Id
  private UUID id;

  private String code;

  private String email;

  @Enumerated(EnumType.STRING)
  private VerificationPurpose purpose;

  @Enumerated(EnumType.STRING)
  private VerificationStatus status;

  public static VerificationJpo fromDomain(Verification verification) {
    return new VerificationJpo(
        verification.getId(),
        verification.getCode().code(),
        verification.getEmail().address(),
        verification.getPurpose(),
        verification.getStatus());
  }

  public Verification toDomain() {
    return new Verification(
        this.getId(),
        new VerificationCode(this.code),
        new Email(this.email),
        this.purpose,
        this.status,
        new DefaultDateTime(this.getCreatedAt(), this.getUpdatedAt()));
  }

  public void updateFromDomain(Verification verification) {
    this.status = verification.getStatus();
  }
}
