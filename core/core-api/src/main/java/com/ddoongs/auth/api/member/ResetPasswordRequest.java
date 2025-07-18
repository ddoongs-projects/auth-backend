package com.ddoongs.auth.api.member;

import com.ddoongs.auth.domain.shared.Email;
import java.util.UUID;

public record ResetPasswordRequest(String email, String password, UUID verificationId) {

  public Email toEmail() {
    return new Email(email);
  }
}
