package com.pdfdancer.client.rest;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for HTTP request retry behavior.
 * <p>
 * This class defines when and how HTTP requests should be retried on failure.
 * It supports configurable retry attempts, exponential backoff, and selective retrying
 * based on HTTP status codes or exception types.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Retry up to 3 times with exponential backoff
 * RetryConfig config = RetryConfig.builder()
 *     .maxAttempts(3)
 *     .initialDelay(Duration.ofMillis(100))
 *     .backoffMultiplier(2.0)
 *     .retryOnStatus(429, 503, 504)
 *     .build();
 * }</pre>
 */
public final class RetryConfig {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final Duration maxDelay;
    private final Set<Integer> retryableStatusCodes;
    private final boolean retryOnTimeout;
    private final boolean retryOnConnectionError;

    private RetryConfig(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelay = builder.initialDelay;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.maxDelay = builder.maxDelay;
        this.retryableStatusCodes = new HashSet<>(builder.retryableStatusCodes);
        this.retryOnTimeout = builder.retryOnTimeout;
        this.retryOnConnectionError = builder.retryOnConnectionError;
    }

    /**
     * Creates a default retry configuration with no retries.
     *
     * @return a RetryConfig with maxAttempts = 1 (no retries)
     */
    public static RetryConfig noRetry() {
        return builder().maxAttempts(1).build();
    }

    /**
     * Creates a default retry configuration suitable for most scenarios.
     * <p>
     * Default settings:
     * <ul>
     *   <li>Max attempts: 3 (2 retries)</li>
     *   <li>Initial delay: 1 second</li>
     *   <li>Backoff multiplier: 2.0 (exponential backoff)</li>
     *   <li>Max delay: 5 seconds</li>
     *   <li>Retryable status codes: 408, 429, 500, 502, 503, 504</li>
     *   <li>Retry on timeout: true</li>
     *   <li>Retry on connection error: true</li>
     * </ul>
     * </p>
     *
     * @return a RetryConfig with sensible defaults
     */
    public static RetryConfig defaultConfig() {
        return builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofSeconds(1))
                .backoffMultiplier(2.0)
                .maxDelay(Duration.ofSeconds(5))
                .retryOnStatus(408, 429, 500, 502, 503, 504)
                .retryOnTimeout(true)
                .retryOnConnectionError(true)
                .build();
    }

    /**
     * Creates a new builder for constructing a RetryConfig.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the maximum number of attempts (including the initial request).
     * A value of 1 means no retries, 2 means 1 retry, etc.
     *
     * @return the maximum number of attempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Gets the initial delay before the first retry.
     *
     * @return the initial delay duration
     */
    public Duration getInitialDelay() {
        return initialDelay;
    }

    /**
     * Gets the backoff multiplier for exponential backoff.
     * Each retry delay is calculated as: initialDelay * (backoffMultiplier ^ attempt)
     *
     * @return the backoff multiplier
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    /**
     * Gets the maximum delay between retry attempts.
     *
     * @return the maximum delay duration
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Gets the set of HTTP status codes that should trigger a retry.
     *
     * @return an unmodifiable set of retryable status codes
     */
    public Set<Integer> getRetryableStatusCodes() {
        return Set.copyOf(retryableStatusCodes);
    }

    /**
     * Determines if requests should be retried on timeout exceptions.
     *
     * @return true if timeouts should trigger retries
     */
    public boolean isRetryOnTimeout() {
        return retryOnTimeout;
    }

    /**
     * Determines if requests should be retried on connection errors (IOException).
     *
     * @return true if connection errors should trigger retries
     */
    public boolean isRetryOnConnectionError() {
        return retryOnConnectionError;
    }

    /**
     * Checks if a specific HTTP status code is retryable.
     *
     * @param statusCode the HTTP status code to check
     * @return true if the status code should trigger a retry
     */
    public boolean isRetryableStatusCode(int statusCode) {
        return retryableStatusCodes.contains(statusCode);
    }

    /**
     * Builder for constructing RetryConfig instances.
     */
    public static final class Builder {
        private int maxAttempts = 1;
        private Duration initialDelay = Duration.ofMillis(100);
        private double backoffMultiplier = 2.0;
        private Duration maxDelay = Duration.ofSeconds(10);
        private Set<Integer> retryableStatusCodes = new HashSet<>();
        private boolean retryOnTimeout = false;
        private boolean retryOnConnectionError = false;

        private Builder() {
        }

        /**
         * Sets the maximum number of attempts (including the initial request).
         * Must be at least 1.
         *
         * @param maxAttempts the maximum number of attempts
         * @return this builder
         * @throws IllegalArgumentException if maxAttempts is less than 1
         */
        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts must be at least 1");
            }
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the initial delay before the first retry.
         *
         * @param initialDelay the initial delay duration
         * @return this builder
         * @throws IllegalArgumentException if initialDelay is null or negative
         */
        public Builder initialDelay(Duration initialDelay) {
            if (initialDelay == null || initialDelay.isNegative()) {
                throw new IllegalArgumentException("initialDelay must be non-null and non-negative");
            }
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * Sets the backoff multiplier for exponential backoff.
         * Must be at least 1.0.
         *
         * @param backoffMultiplier the backoff multiplier
         * @return this builder
         * @throws IllegalArgumentException if backoffMultiplier is less than 1.0
         */
        public Builder backoffMultiplier(double backoffMultiplier) {
            if (backoffMultiplier < 1.0) {
                throw new IllegalArgumentException("backoffMultiplier must be at least 1.0");
            }
            this.backoffMultiplier = backoffMultiplier;
            return this;
        }

        /**
         * Sets the maximum delay between retry attempts.
         *
         * @param maxDelay the maximum delay duration
         * @return this builder
         * @throws IllegalArgumentException if maxDelay is null or negative
         */
        public Builder maxDelay(Duration maxDelay) {
            if (maxDelay == null || maxDelay.isNegative()) {
                throw new IllegalArgumentException("maxDelay must be non-null and non-negative");
            }
            this.maxDelay = maxDelay;
            return this;
        }

        /**
         * Adds HTTP status codes that should trigger a retry.
         *
         * @param statusCodes the status codes to add
         * @return this builder
         */
        public Builder retryOnStatus(int... statusCodes) {
            for (int code : statusCodes) {
                this.retryableStatusCodes.add(code);
            }
            return this;
        }

        /**
         * Sets whether requests should be retried on timeout exceptions.
         *
         * @param retryOnTimeout true to retry on timeouts
         * @return this builder
         */
        public Builder retryOnTimeout(boolean retryOnTimeout) {
            this.retryOnTimeout = retryOnTimeout;
            return this;
        }

        /**
         * Sets whether requests should be retried on connection errors.
         *
         * @param retryOnConnectionError true to retry on connection errors
         * @return this builder
         */
        public Builder retryOnConnectionError(boolean retryOnConnectionError) {
            this.retryOnConnectionError = retryOnConnectionError;
            return this;
        }

        /**
         * Builds the RetryConfig instance.
         *
         * @return a new RetryConfig instance
         */
        public RetryConfig build() {
            return new RetryConfig(this);
        }
    }
}
