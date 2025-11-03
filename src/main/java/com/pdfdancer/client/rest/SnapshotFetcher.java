package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.response.DocumentSnapshot;
import com.pdfdancer.common.response.PageSnapshot;

interface SnapshotFetcher {
    DocumentSnapshot fetchDocumentSnapshot(String types);

    PageSnapshot fetchPageSnapshot(int pageIndex, String types);

    <T extends ObjectRef> TypedDocumentSnapshot<T> fetchTypedDocumentSnapshot(Class<T> elementClass, String types);

    <T extends ObjectRef> TypedPageSnapshot<T> fetchTypedPageSnapshot(int pageIndex, Class<T> elementClass, String types);
}
