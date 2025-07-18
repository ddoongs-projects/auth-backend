package com.ddoongs.auth.restdocs;

import com.ddoongs.auth.domain.shared.CoreErrorCode;

public record ApiErrorDetail(CoreErrorCode code, String cause) {}
