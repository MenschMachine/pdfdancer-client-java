package com.pdfdancer.client.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class AnonTokenResponse {
    @JsonProperty("token")
    private final String token;
    @JsonProperty("metadata")
    private final ApiTokenMetadata metadata;

    @JsonCreator
    public AnonTokenResponse(
            @JsonProperty("token") String token,           // Raw token - only shown once
            @JsonProperty("metadata") ApiTokenMetadata metadata
    ) {
        this.token = token;
        this.metadata = metadata;
    }

    public String token() {
        return token;
    }

    public ApiTokenMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AnonTokenResponse) obj;
        return Objects.equals(this.token, that.token) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, metadata);
    }

    @Override
    public String toString() {
        return "AnonTokenResponse[" +
                "token=" + token + ", " +
                "metadata=" + metadata + ']';
    }

    public static final class ApiTokenMetadata {
        @JsonProperty("id")
        private final String id;
        @JsonProperty("name")
        private final String name;
        @JsonProperty("prefix")
        private final String prefix;
        @JsonProperty("createdAt")
        private final String createdAt;
        @JsonProperty("expiresAt")
        private final String expiresAt;

        @JsonCreator
        public ApiTokenMetadata(
                @JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("prefix") String prefix,
                @JsonProperty("createdAt") String createdAt,
                @JsonProperty("expiresAt") String expiresAt
        ) {
            this.id = id;
            this.name = name;
            this.prefix = prefix;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
        }

        public String id() {
            return id;
        }

        public String name() {
            return name;
        }

        public String prefix() {
            return prefix;
        }

        public String createdAt() {
            return createdAt;
        }

        public String expiresAt() {
            return expiresAt;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ApiTokenMetadata) obj;
            return Objects.equals(this.id, that.id) &&
                    Objects.equals(this.name, that.name) &&
                    Objects.equals(this.prefix, that.prefix) &&
                    Objects.equals(this.createdAt, that.createdAt) &&
                    Objects.equals(this.expiresAt, that.expiresAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, prefix, createdAt, expiresAt);
        }

        @Override
        public String toString() {
            return "ApiTokenMetadata[" +
                    "id=" + id + ", " +
                    "name=" + name + ", " +
                    "prefix=" + prefix + ", " +
                    "createdAt=" + createdAt + ", " +
                    "expiresAt=" + expiresAt + ']';
        }

    }
}
