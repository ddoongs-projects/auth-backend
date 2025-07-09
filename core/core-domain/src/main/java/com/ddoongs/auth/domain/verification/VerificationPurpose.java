package com.ddoongs.auth.domain.verification;

import lombok.Getter;

@Getter
public enum VerificationPurpose {
  REGISTER("회원가입"),
  RESET_PASSWORD("비밀번호 초기화");

  private final String description;

  VerificationPurpose(String description) {
    this.description = description;
  }
}
