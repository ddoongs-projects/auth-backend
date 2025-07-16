package com.ddoongs.auth.domain.token;

public record TokenPair(String accessToken, RefreshToken refreshToken) {}
