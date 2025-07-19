package com.ddoongs.auth.restdocs;

public record ApiErrorWithCause(String code, String message, String cause) {}
