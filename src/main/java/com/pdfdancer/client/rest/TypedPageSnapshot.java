package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.PageRef;

import java.util.List;

/**
 * A typed snapshot of a single page containing elements of type T.
 */
public final class TypedPageSnapshot<T extends ObjectRef> {
    private PageRef pageRef;
    private List<T> elements;

    public TypedPageSnapshot() {
    }

    public TypedPageSnapshot(PageRef pageRef, List<T> elements) {
        this.pageRef = pageRef;
        this.elements = elements;
    }

    public PageRef getPageRef() {
        return pageRef;
    }

    public void setPageRef(PageRef pageRef) {
        this.pageRef = pageRef;
    }

    public List<T> getElements() {
        return elements;
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
    }
}
