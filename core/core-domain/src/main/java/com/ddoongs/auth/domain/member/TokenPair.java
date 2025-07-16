package com.ddoongs.auth.domain.member;

public record TokenPair(String accessToken, RefreshToken refreshToken) {}
