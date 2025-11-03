package com.tfc.pdf.pdfdancer.api.client.http;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static Builder builder() {
        return new Builder();
    }

    public List<Part> parts() {
        return parts;
    }

    public String boundary() {
        return boundary;
    }

    public static final class Part {
        private final String name;
        private final String fileName;
        private final MediaType contentType;
        private final byte[] content;

        public Part(String name, String fileName, MediaType contentType, byte[] content) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(contentType, "contentType");
            Objects.requireNonNull(content, "content");
            this.name = name;
            this.fileName = fileName;
            this.contentType = contentType;
            this.content = content;
        }

        public static Part forText(String name, String value) {
            return new Part(name, null, MediaType.TEXT_PLAIN_TYPE, value.getBytes(StandardCharsets.UTF_8));
        }

        public String name() {
            return name;
        }

        public String fileName() {
            return fileName;
        }

        public MediaType contentType() {
            return contentType;
        }

        public byte[] content() {
            return content;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Part that = (Part) obj;
            return Objects.equals(this.name, that.name)
                    && Objects.equals(this.fileName, that.fileName)
                    && Objects.equals(this.contentType, that.contentType)
                    && Arrays.equals(this.content, that.content);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(name, fileName, contentType);
            result = 31 * result + Arrays.hashCode(content);
            return result;
        }

        @Override
        public String toString() {
            return "Part[" +
                    "name=" + name + ", " +
                    "fileName=" + fileName + ", " +
                    "contentType=" + contentType + ", " +
                    "content=" + Arrays.toString(content) + ']';
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