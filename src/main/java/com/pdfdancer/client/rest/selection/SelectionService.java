package com.pdfdancer.client.rest.selection;

import com.pdfdancer.client.rest.PDFDancer;
import com.pdfdancer.client.rest.TypedDocumentSnapshot;
import com.pdfdancer.client.rest.TypedPageSnapshot;
import com.pdfdancer.common.model.*;
import com.pdfdancer.common.response.DocumentSnapshot;
import com.pdfdancer.common.response.PageSnapshot;

import java.util.*;

public class SelectionService {

    public boolean containsPoint(ObjectRef ref, double x, double y, double epsilon) {
        Position position = ref.getPosition();
        if (position == null || position.getBoundingRect() == null) {
            return false;
        }
        BoundingRect rect = position.getBoundingRect();
        double rectX = rect.getX();
        double rectY = rect.getY();
        double rectWidth = rect.getWidth();
        double rectHeight = rect.getHeight();
        double minX = rectX - epsilon;
        double maxX = rectX + rectWidth + epsilon;
        double minY = rectY - epsilon;
        double maxY = rectY + rectHeight + epsilon;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null || prefix == null) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public List<ObjectRef> collectAllElements(DocumentSnapshot snapshot) {
        List<ObjectRef> results = new ArrayList<>();
        if (snapshot == null || snapshot.pages() == null) {
            return results;
        }
        for (PageSnapshot page : snapshot.pages()) {
            if (page == null || page.elements() == null) {
                continue;
            }
            for (ObjectRef element : page.elements()) {
                if (element != null && element.getType() != null) {
                    results.add(element);
                }
            }
        }
        return results;
    }

    public List<ObjectRef> collectObjectsByType(DocumentSnapshot snapshot, Set<ObjectType> types) {
        List<ObjectRef> results = new ArrayList<>();
        if (snapshot == null || snapshot.pages() == null) {
            return results;
        }
        for (PageSnapshot page : snapshot.pages()) {
            accumulateObjectsByType(results, page, types);
        }
        return results;
    }

    public List<ObjectRef> collectObjectsByType(PageSnapshot snapshot, Set<ObjectType> types) {
        List<ObjectRef> results = new ArrayList<>();
        accumulateObjectsByType(results, snapshot, types);
        return results;
    }

    private void accumulateObjectsByType(List<ObjectRef> target, PageSnapshot snapshot, Set<ObjectType> types) {
        if (snapshot == null || snapshot.elements() == null) {
            return;
        }
        for (ObjectRef element : snapshot.elements()) {
            if (element == null) {
                continue;
            }
            ObjectType elementType = element.getType();
            if (elementType == null) {
                continue;
            }
            if (types.contains(elementType)) {
                target.add(element);
            }
        }
    }

    public <T extends ObjectRef> List<T> getTypedElements(TypedPageSnapshot<T> page, Class<T> elementClass) {
        List<T> rawElements = page.getElements();
        if (rawElements == null || rawElements.isEmpty()) {
            return Collections.emptyList();
        }
        for (Object element : rawElements) {
            if (!elementClass.isInstance(element)) {
                throw new IllegalStateException("Expected elements of type " + elementClass.getName() + " but got " + element.getClass().getName());
            }
        }
        return rawElements;
    }

    public <T extends ObjectRef> List<T> flattenTypedDocument(TypedDocumentSnapshot<T> snapshot, Class<T> elementClass) {
        List<T> results = new ArrayList<>();
        for (TypedPageSnapshot<T> page : snapshot.getPages()) {
            List<T> elements = getTypedElements(page, elementClass);
            results.addAll(elements);
        }
        return results;
    }

    public List<FormFieldRef> collectFormFieldRefsFromDocument(PDFDancer root) {
        List<FormFieldRef> results = new ArrayList<>();
        for (Form.FormType filter : Form.FormType.values()) {
            TypedDocumentSnapshot<FormFieldRef> snapshot = root.getTypedDocumentSnapshot(FormFieldRef.class, filter.name());
            List<FormFieldRef> elements = flattenTypedDocument(snapshot, FormFieldRef.class);
            elements.stream()
                    .map(ref -> adjustFormFieldType(ref, filter))
                    .filter(Objects::nonNull)
                    .forEach(results::add);
        }
        return results;
    }

    public List<FormFieldRef> collectFormFieldRefsFromPage(PDFDancer root, int pageNumber) {
        List<FormFieldRef> results = new ArrayList<>();
        for (Form.FormType filter : Form.FormType.values()) {
            TypedPageSnapshot<FormFieldRef> snapshot = root.getTypedPageSnapshot(pageNumber, FormFieldRef.class, filter.name());
            List<FormFieldRef> elements = getTypedElements(snapshot, FormFieldRef.class);
            elements.stream()
                    .map(ref -> adjustFormFieldType(ref, filter))
                    .filter(Objects::nonNull)
                    .forEach(results::add);
        }
        return results;
    }

    public FormFieldRef adjustFormFieldType(FormFieldRef ref, Form.FormType filter) {
        if (ref == null) {
            return null;
        }
        ObjectType desiredType;
        try {
            desiredType = ObjectType.valueOf(filter.name());
        } catch (IllegalArgumentException ex) {
            desiredType = ref.getType();
        }
        if (desiredType == null || desiredType == ref.getType()) {
            return ref;
        }
        return new FormFieldRef(ref.getInternalId(), ref.getPosition(), desiredType, ref.getObjectRefType(), ref.getName(), ref.getValue());
    }
}
