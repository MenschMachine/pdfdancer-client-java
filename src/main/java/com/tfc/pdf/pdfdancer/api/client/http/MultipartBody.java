package com.tfc.pdf.pdfdancer.api.client.http;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple multipart body representation used for file uploads.
 */
public final class MultipartBody {

    private final List<Part> parts;
    private final String boundary;

    private MultipartBody(List<Part> parts, String boundary) {
        this.parts = parts;
        this.boundary = boundary;
    }

    public List<Part> parts() {
        return parts;
    }

    public String boundary() {
        return boundary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public record Part(String name, String fileName, MediaType contentType, byte[] content) {
        public Part {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(contentType, "contentType");
            Objects.requireNonNull(content, "content");
        }

        public static Part forText(String name, String value) {
            return new Part(name, null, MediaType.TEXT_PLAIN_TYPE, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static final class Builder {
        private final List<Part> parts = new ArrayList<>();

        public Builder addPart(String name, String fileName, MediaType contentType, byte[] content) {
            parts.add(new Part(name, fileName, contentType, content));
            return this;
        }

        public Builder addPart(String name, String value) {
            parts.add(Part.forText(name, value));
            return this;
        }

        public MultipartBody build() {
            if (parts.isEmpty()) {
                throw new IllegalStateException("Multipart body must contain at least one part");
            }
            String boundary = "----PdfDancerBoundary" + UUID.randomUUID();
            return new MultipartBody(Collections.unmodifiableList(new ArrayList<>(parts)), boundary);
        }
    }
}
