package com.ddoongs.auth.infrastructure.mail;

import com.ddoongs.auth.domain.verification.VerificationCode;
import com.ddoongs.auth.domain.verification.VerificationPurpose;

public interface EmailVerificationCodeHtmlLoader {

  String loadWith(VerificationCode code, VerificationPurpose purpose);
}
