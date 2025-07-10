package com.ddoongs.auth.domain.verification;

public interface VerificationSender {

  void send(Verification verification);
}
