package com.ddoongs.auth.domain.verification;

import com.ddoongs.auth.domain.shared.Email;

public record CreateVerification(Email email, VerificationPurpose purpose) {}
