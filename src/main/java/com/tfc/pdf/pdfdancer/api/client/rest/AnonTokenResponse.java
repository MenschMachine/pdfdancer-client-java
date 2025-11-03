package com.tfc.pdf.pdfdancer.api.client.rest;
public record AnonTokenResponse(
        String token,           // Raw token - only shown once
        ApiTokenMetadata metadata
) {
    public record ApiTokenMetadata(
            String id,
            String name,
            String prefix,
            String createdAt,
            String expiresAt
    ) {
    }
}
