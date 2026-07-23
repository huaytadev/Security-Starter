package com.security_starter.common.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> validationErrors,
        Instant timestamp
){}
