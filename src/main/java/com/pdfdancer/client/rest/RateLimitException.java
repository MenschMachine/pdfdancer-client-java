package com.pdfdancer.client.rest;

import java.time.Duration;

/** HTTP 429 response after the configured attempts are exhausted. */
public class RateLimitException extends HttpClientException {
    private final Duration retryAfter;
    public RateLimitException(String message, Duration retryAfter) {
        super(429, message);
        this.retryAfter = retryAfter;
    }
    public Duration getRetryAfter() { return retryAfter; }
}
