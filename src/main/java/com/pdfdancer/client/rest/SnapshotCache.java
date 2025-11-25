package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.response.DocumentSnapshot;
import com.pdfdancer.common.response.PageSnapshot;
import com.pdfdancer.client.http.HttpRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulates all snapshot fetching and caching logic.
 */
final class SnapshotCache implements SnapshotFetcher {
    private static final String ALL_TYPES_KEY = "__ALL__";

    private final String token;
    private final String sessionId;
    private final PdfDancerHttpClient.Blocking blockingClient;

    private final Map<String, DocumentSnapshot> documentSnapshotCache = new HashMap<>();
    private final Map<PageSnapshotKey, PageSnapshot> pageSnapshotCache = new HashMap<>();
    private final Map<DocumentSnapshotKey, TypedDocumentSnapshot<?>> typedDocumentSnapshotCache = new HashMap<>();
    private final Map<TypedPageSnapshotKey, TypedPageSnapshot<?>> typedPageSnapshotCache = new HashMap<>();

    SnapshotCache(String token, String sessionId, PdfDancerHttpClient.Blocking blockingClient) {
        this.token = token;
        this.sessionId = sessionId;
        this.blockingClient = blockingClient;
    }

    void invalidate() {
        documentSnapshotCache.clear();
        pageSnapshotCache.clear();
        typedDocumentSnapshotCache.clear();
        typedPageSnapshotCache.clear();
    }

    private String normalizeTypes(String types) {
        if (types == null || types.isBlank()) {
            return ALL_TYPES_KEY;
        }
        String normalized = Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.joining(","));
        return normalized.isBlank() ? ALL_TYPES_KEY : normalized;
    }

    @Override
    public DocumentSnapshot fetchDocumentSnapshot(String types) {
        String path = "/pdf/document/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                DocumentSnapshot.class
        );
    }

    @Override
    public PageSnapshot fetchPageSnapshot(int pageNumber, String types) {
        String path = "/pdf/page/" + pageNumber + "/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        return blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                PageSnapshot.class
        );
    }

    @Override
    public <T extends ObjectRef> TypedDocumentSnapshot<T> fetchTypedDocumentSnapshot(Class<T> elementClass, String types) {
        String path = "/pdf/document/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        @SuppressWarnings("unchecked")
        TypedDocumentSnapshot<T> result = blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                TypedDocumentSnapshot.class
        );
        return result;
    }

    @Override
    public <T extends ObjectRef> TypedPageSnapshot<T> fetchTypedPageSnapshot(int pageNumber, Class<T> elementClass, String types) {
        String path = "/pdf/page/" + pageNumber + "/snapshot";
        if (types != null && !types.isBlank()) {
            path += "?types=" + types;
        }
        @SuppressWarnings("unchecked")
        TypedPageSnapshot<T> result = blockingClient.retrieve(
                HttpRequest.GET(path)
                        .bearerAuth(token)
                        .header("X-Session-Id", sessionId),
                TypedPageSnapshot.class
        );
        return result;
    }

    DocumentSnapshot getDocumentSnapshotCached(String types) {
        String key = normalizeTypes(types);
        DocumentSnapshot cached = documentSnapshotCache.get(key);
        if (cached != null) return cached;
        DocumentSnapshot snapshot = fetchDocumentSnapshot(types);
        documentSnapshotCache.put(key, snapshot);
        List<PageSnapshot> pages = snapshot.pages();
        for (int i = 0; i < pages.size(); i++) {
            pageSnapshotCache.put(new PageSnapshotKey(i, key), pages.get(i));
        }
        return snapshot;
    }

    PageSnapshot getPageSnapshotCached(int pageNumber, String types) {
        String key = normalizeTypes(types);
        PageSnapshotKey cacheKey = new PageSnapshotKey(pageNumber, key);
        PageSnapshot cached = pageSnapshotCache.get(cacheKey);
        if (cached != null) return cached;
        PageSnapshot snapshot = fetchPageSnapshot(pageNumber, types);
        pageSnapshotCache.put(cacheKey, snapshot);
        return snapshot;
    }

    <T extends ObjectRef> TypedDocumentSnapshot<T> getTypedDocumentSnapshot(Class<T> elementClass, String types) {
        String key = normalizeTypes(types);
        DocumentSnapshotKey cacheKey = new DocumentSnapshotKey(elementClass, key);
        @SuppressWarnings("unchecked")
        TypedDocumentSnapshot<T> cached = (TypedDocumentSnapshot<T>) typedDocumentSnapshotCache.get(cacheKey);
        if (cached != null) return cached;
        TypedDocumentSnapshot<T> snapshot = fetchTypedDocumentSnapshot(elementClass, types);
        typedDocumentSnapshotCache.put(cacheKey, snapshot);
        List<TypedPageSnapshot<T>> pages = snapshot.getPages();
        for (int i = 0; i < pages.size(); i++) {
            typedPageSnapshotCache.put(new TypedPageSnapshotKey(i, elementClass, key), pages.get(i));
        }
        return snapshot;
    }

    <T extends ObjectRef> TypedPageSnapshot<T> getTypedPageSnapshot(int pageNumber, Class<T> elementClass, String types) {
        String key = normalizeTypes(types);
        TypedPageSnapshotKey cacheKey = new TypedPageSnapshotKey(pageNumber, elementClass, key);
        @SuppressWarnings("unchecked")
        TypedPageSnapshot<T> cached = (TypedPageSnapshot<T>) typedPageSnapshotCache.get(cacheKey);
        if (cached != null) return cached;
        TypedPageSnapshot<T> snapshot = fetchTypedPageSnapshot(pageNumber, elementClass, types);
        typedPageSnapshotCache.put(cacheKey, snapshot);
        return snapshot;
    }
}
