package com.ddoongs.auth.domain.token;

import com.ddoongs.auth.domain.member.Provider;

public record PrepareTokenExchange(Provider provider, String providerId) {}
