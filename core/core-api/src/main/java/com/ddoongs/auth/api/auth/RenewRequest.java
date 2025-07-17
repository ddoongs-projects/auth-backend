package com.ddoongs.auth.api.auth;

import jakarta.validation.constraints.NotNull;

public record RenewRequest(@NotNull String refreshToken) {}
