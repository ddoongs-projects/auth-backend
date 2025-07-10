package com.ddoongs.auth.infrastructure.mail;

import com.ddoongs.auth.domain.verification.VerificationCode;
import org.springframework.stereotype.Component;

@Component
public interface EmailVerificationCodeHtmlLoader {

  String loadWith(VerificationCode code);
}
