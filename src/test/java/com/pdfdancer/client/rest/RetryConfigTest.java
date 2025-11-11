package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RetryConfig}.
 */
class RetryConfigTest {

    @Test
    void testNoRetry() {
        RetryConfig config = RetryConfig.noRetry();

        assertEquals(1, config.getMaxAttempts());
        assertFalse(config.isRetryOnTimeout());
        assertFalse(config.isRetryOnConnectionError());
        assertTrue(config.getRetryableStatusCodes().isEmpty());
    }

    @Test
    void testDefaultConfig() {
        RetryConfig config = RetryConfig.defaultConfig();

        assertEquals(3, config.getMaxAttempts());
        assertEquals(Duration.ofSeconds(1), config.getInitialDelay());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(5), config.getMaxDelay());
        assertTrue(config.isRetryOnTimeout());
        assertTrue(config.isRetryOnConnectionError());

        Set<Integer> statusCodes = config.getRetryableStatusCodes();
        assertTrue(statusCodes.contains(408)); // Request Timeout
        assertTrue(statusCodes.contains(429)); // Too Many Requests
        assertTrue(statusCodes.contains(500)); // Internal Server Error
        assertTrue(statusCodes.contains(502)); // Bad Gateway
        assertTrue(statusCodes.contains(503)); // Service Unavailable
        assertTrue(statusCodes.contains(504)); // Gateway Timeout
    }

    @Test
    void testBuilder() {
        RetryConfig config = RetryConfig.builder()
                .maxAttempts(5)
                .initialDelay(Duration.ofMillis(200))
                .backoffMultiplier(3.0)
                .maxDelay(Duration.ofSeconds(10))
                .retryOnStatus(500, 503)
                .retryOnTimeout(true)
                .retryOnConnectionError(false)
                .build();

        assertEquals(5, config.getMaxAttempts());
        assertEquals(Duration.ofMillis(200), config.getInitialDelay());
        assertEquals(3.0, config.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(10), config.getMaxDelay());
        assertTrue(config.isRetryOnTimeout());
        assertFalse(config.isRetryOnConnectionError());

        assertTrue(config.isRetryableStatusCode(500));
        assertTrue(config.isRetryableStatusCode(503));
        assertFalse(config.isRetryableStatusCode(502));
        assertFalse(config.isRetryableStatusCode(404));
    }

    @Test
    void testBuilderMultipleRetryOnStatus() {
        RetryConfig config = RetryConfig.builder()
                .retryOnStatus(500, 502, 503)
                .retryOnStatus(429)
                .build();

        assertTrue(config.isRetryableStatusCode(500));
        assertTrue(config.isRetryableStatusCode(502));
        assertTrue(config.isRetryableStatusCode(503));
        assertTrue(config.isRetryableStatusCode(429));
        assertFalse(config.isRetryableStatusCode(404));
    }

    @Test
    void testBuilderValidation_maxAttemptsMustBeAtLeastOne() {
        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().maxAttempts(0).build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().maxAttempts(-1).build()
        );
    }

    @Test
    void testBuilderValidation_initialDelayMustBeNonNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().initialDelay(Duration.ofMillis(-1)).build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().initialDelay(null).build()
        );
    }

    @Test
    void testBuilderValidation_backoffMultiplierMustBeAtLeastOne() {
        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().backoffMultiplier(0.5).build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().backoffMultiplier(0.0).build()
        );
    }

    @Test
    void testBuilderValidation_maxDelayMustBeNonNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().maxDelay(Duration.ofMillis(-1)).build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                RetryConfig.builder().maxDelay(null).build()
        );
    }

    @Test
    void testRetryableStatusCodesImmutable() {
        RetryConfig config = RetryConfig.builder()
                .retryOnStatus(500, 503)
                .build();

        Set<Integer> statusCodes = config.getRetryableStatusCodes();
        assertThrows(UnsupportedOperationException.class, () ->
                statusCodes.add(502)
        );
    }

    @Test
    void testBuilderDefaults() {
        RetryConfig config = RetryConfig.builder().build();

        assertEquals(1, config.getMaxAttempts());
        assertEquals(Duration.ofMillis(100), config.getInitialDelay());
        assertEquals(2.0, config.getBackoffMultiplier());
        assertEquals(Duration.ofSeconds(10), config.getMaxDelay());
        assertFalse(config.isRetryOnTimeout());
        assertFalse(config.isRetryOnConnectionError());
        assertTrue(config.getRetryableStatusCodes().isEmpty());
    }

    @Test
    void testZeroInitialDelay() {
        RetryConfig config = RetryConfig.builder()
                .initialDelay(Duration.ZERO)
                .build();

        assertEquals(Duration.ZERO, config.getInitialDelay());
    }

    @Test
    void testBackoffMultiplierExactlyOne() {
        RetryConfig config = RetryConfig.builder()
                .backoffMultiplier(1.0)
                .build();

        assertEquals(1.0, config.getBackoffMultiplier());
    }
}
