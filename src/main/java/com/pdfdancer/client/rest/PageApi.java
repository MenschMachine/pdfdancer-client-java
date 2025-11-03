package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
import com.pdfdancer.common.response.PageSnapshot;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Page-scoped operations extracted from PDFDancer.PageClient.
 * PDFDancer.PageClient becomes a thin wrapper extending this class to preserve API.
 */
public class PageApi {
    protected final PDFDancer root;
    protected final int pageIndex;

    public PageApi(PDFDancer root, int pageIndex) {
        this.root = root;
        this.pageIndex = pageIndex;
    }

    /**
     * Selects all paragraph objects on this page.
     */
    public List<TextParagraphReference> selectParagraphs() {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(typed);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public List<TextParagraphReference> selectParagraphsStartingWith(String text) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(
                typed.stream()
                        .filter(ref -> root.startsWithIgnoreCase(ref.getText(), text))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<TextParagraphReference> selectParagraphsAt(double x, double y) {
        return selectParagraphsAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<TextParagraphReference> selectParagraphsAt(double x, double y, double epsilon) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(
                typed.stream()
                        .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<TextParagraphReference> selectParagraphsMatching(String pattern) {
        Pattern compiled = Pattern.compile(pattern, Pattern.DOTALL);
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(
                typed.stream()
                        .filter(ref -> ref.getText() != null && compiled.matcher(ref.getText()).matches())
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<PathReference> selectPathAt(double x, double y) {
        Position position = new PositionBuilder().onPage(pageIndex).atCoordinates(x, y).build();
        return root.toPathObject(root.find(ObjectType.PATH, position));
    }

    public List<TextLineReference> selectTextLinesStartingWith(String text) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_TEXT_LINE);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextLineObject(
                typed.stream()
                        .filter(ref -> root.startsWithIgnoreCase(ref.getText(), text))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<TextLineReference> selectTextLineAt(double x, double y) {
        return selectTextLineAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<TextLineReference> selectTextLines() {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_TEXT_LINE);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextLineObject(typed);
    }

    public List<TextLineReference> selectTextLineAt(double x, double y, double epsilon) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_TEXT_LINE);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextLineObject(
                typed.stream()
                        .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<ImageReference> selectImages() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        return root.toImageObject(images);
    }

    public List<ImageReference> selectImagesAt(double x, double y) {
        return selectImagesAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<ImageReference> selectImagesAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        List<ObjectRef> filtered = images.stream()
                .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                .collect(Collectors.toUnmodifiableList());
        return root.toImageObject(filtered);
    }

    public List<FormXObjectReference> selectForms() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
        return root.toFormXObject(forms);
    }

    public List<PathReference> selectPaths() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.PATH));
        return root.toPathObject(forms);
    }

    public List<FormXObjectReference> selectFormsAt(double x, double y) {
        return selectFormsAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<FormXObjectReference> selectFormsAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
        List<ObjectRef> filtered = forms.stream()
                .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                .collect(Collectors.toUnmodifiableList());
        return root.toFormXObject(filtered);
    }

    public List<FormFieldReference> selectFormFields() {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageIndex);
        return root.toFormFieldObject(formFields);
    }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y) {
        return selectFormFieldsAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y, double epsilon) {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageIndex);
        return root.toFormFieldObject(
                formFields.stream()
                        .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<TextParagraphReference> selectTextStartingWith(String text) {
        return this.selectParagraphsStartingWith(text);
    }

    public BezierBuilder newBezier() {
        return new BezierBuilder(root, pageIndex);
    }

    public PathBuilder newPath() {
        return new PathBuilder(root, pageIndex);
    }

    public LineBuilder newLine() {
        return new LineBuilder(root, pageIndex);
    }
}
