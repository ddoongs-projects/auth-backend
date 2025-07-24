package com.ddoongs.auth.api.auth;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TokenExchangeRequest(@NotNull UUID authCode) {}
