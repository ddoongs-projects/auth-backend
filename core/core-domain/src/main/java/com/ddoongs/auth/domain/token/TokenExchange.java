package com.ddoongs.auth.domain.token;

import java.util.UUID;

public record TokenExchange(UUID authCode, TokenPair tokenPair) {}
