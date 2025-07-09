package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Verification {

  private final UUID id;
  private VerificationCode code;
  private Email email;
  private VerificationPurpose purpose;
  private VerificationStatus status;
  private DefaultDateTime defaultDateTime;

  private Verification() {
    this.id = UUID.randomUUID();
  }

  public static Verification create(String code, Email email, VerificationPurpose purpose) {
    Verification verification = new Verification();

    verification.code = new VerificationCode(code);
    verification.email = email;
    verification.purpose = purpose;

    verification.status = VerificationStatus.PENDING;

    return verification;
  }
}
