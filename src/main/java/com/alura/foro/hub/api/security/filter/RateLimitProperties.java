package com.alura.foro.hub.api.security.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ratelimit")
public record RateLimitProperties(
        boolean enabled,
        int loginMax,
        int writeMax,
        int readMax,
        long windowSeconds
) {}

