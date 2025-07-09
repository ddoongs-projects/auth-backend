package com.ddoongs.auth.domain;

import java.util.UUID;
import lombok.Getter;

@Getter
public class VerificationCode {

  private final UUID id;
  private VerificationNumber code;
  private Email email;
  private VerificationPurpose purpose;
  private VerificationCodeStatus status;
  private DefaultDateTime defaultDateTime;

  private VerificationCode() {
    this.id = UUID.randomUUID();
  }

  public static VerificationCode create(String code, Email email, VerificationPurpose purpose) {
    VerificationCode verificationCode = new VerificationCode();

    verificationCode.code = new VerificationNumber(code);
    verificationCode.email = email;
    verificationCode.purpose = purpose;

    verificationCode.status = VerificationCodeStatus.PENDING;

    return verificationCode;
  }
}
