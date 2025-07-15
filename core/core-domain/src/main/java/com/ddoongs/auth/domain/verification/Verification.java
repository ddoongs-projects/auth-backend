package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.DefaultDateTime;
import com.ddoongs.auth.domain.shared.Email;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
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

  public static Verification create(
      CreateVerification createVerification, VerificationCodeGenerator verificationCodeGenerator) {
    Verification verification = new Verification();

    verification.code = verificationCodeGenerator.generate();
    verification.email = createVerification.email();
    verification.purpose = createVerification.purpose();

    verification.status = VerificationStatus.PENDING;

    verification.defaultDateTime = DefaultDateTime.now();

    return verification;
  }

  public void verify(VerificationCode code) {
    if (this.status == VerificationStatus.VERIFIED) {
      throw new VerificationAlreadyCompletedException();
    }

    if (!this.code.equals(code)) {
      throw new InvalidVerificationCodeException();
    }

    this.status = VerificationStatus.VERIFIED;
  }

  /**
   * 주어진 이메일·인증목적에 대한 유효성을 검증한다.
   *
   * @throws VerificationMismatchException     해당되지 않은 인증일 경우
   * @throws VerificationNotCompletedException 인증이 완료되지 않았을 경우
   */
  public void ensureValidFor(Email expectedEmail, VerificationPurpose expectedPurpose) {
    if (!this.email.equals(expectedEmail) || this.purpose != expectedPurpose) {
      throw new VerificationMismatchException();
    }

    if (this.status != VerificationStatus.VERIFIED) {
      throw new VerificationNotCompletedException();
    }
  }
}
