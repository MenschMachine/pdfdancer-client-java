package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
import com.pdfdancer.common.response.PageSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Top-level implementation of page-scoped operations extracted from PDFDancer.PageClient.
 * The nested PDFDancer.PageClient will extend this class to preserve the public API type.
 */
class PageClientImpl {
    protected final PDFDancer root;
    protected final int pageIndex;

    PageClientImpl(PDFDancer root, int pageIndex) {
        this.root = root;
        this.pageIndex = pageIndex;
    }

    public List<TextParagraphReference> selectParagraphs() {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(typed);
    }

    public int getPageIndex() { return pageIndex; }

    public List<TextParagraphReference> selectParagraphsStartingWith(String text) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(
                typed.stream()
                        .filter(ref -> root.startsWithIgnoreCase(ref.getText(), text))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    public List<TextParagraphReference> selectParagraphsAt(double x, double y) { return selectParagraphsAt(x, y, PDFDancer.DEFAULT_EPSILON); }

    public List<TextParagraphReference> selectParagraphsAt(double x, double y, double epsilon) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_PARAGRAPH);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextObject(
                typed.stream()
                        .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Selects a single paragraph at the specified coordinates with default epsilon.
     * @return Optional containing the first paragraph found at the position, or empty if none found
     */
    public Optional<TextParagraphReference> selectParagraphAt(double x, double y) {
        return selectParagraphAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    /**
     * Selects a single paragraph at the specified coordinates with custom epsilon tolerance.
     * @return Optional containing the first paragraph found at the position, or empty if none found
     */
    public Optional<TextParagraphReference> selectParagraphAt(double x, double y, double epsilon) {
        List<TextParagraphReference> paragraphs = selectParagraphsAt(x, y, epsilon);
        return paragraphs.isEmpty() ? Optional.empty() : Optional.of(paragraphs.get(0));
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

    public List<PathReference> selectPathsAt(double x, double y) {
        Position position = new PositionBuilder().onPage(pageIndex).atCoordinates(x, y).build();
        return root.toPathObject(root.find(ObjectType.PATH, position));
    }

    /**
     * Selects a single path at the specified coordinates.
     * @return Optional containing the first path found at the position, or empty if none found
     */
    public Optional<PathReference> selectPathAt(double x, double y) {
        List<PathReference> paths = selectPathsAt(x, y);
        return paths.isEmpty() ? Optional.empty() : Optional.of(paths.get(0));
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

    public List<TextLineReference> selectTextLinesAt(double x, double y) { return selectTextLinesAt(x, y, PDFDancer.DEFAULT_EPSILON); }

    public List<TextLineReference> selectTextLines() {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_TEXT_LINE);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextLineObject(typed);
    }

    public List<TextLineReference> selectTextLinesAt(double x, double y, double epsilon) {
        TypedPageSnapshot<TextTypeObjectRef> snapshot = root.getTypedPageSnapshot(pageIndex, TextTypeObjectRef.class, PDFDancer.TYPES_TEXT_LINE);
        List<TextTypeObjectRef> typed = root.getTypedElements(snapshot, TextTypeObjectRef.class);
        return root.toTextLineObject(
                typed.stream()
                        .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Selects a single text line at the specified coordinates with default epsilon.
     * @return Optional containing the first text line found at the position, or empty if none found
     */
    public Optional<TextLineReference> selectTextLineAt(double x, double y) {
        return selectTextLineAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    /**
     * Selects a single text line at the specified coordinates with custom epsilon tolerance.
     * @return Optional containing the first text line found at the position, or empty if none found
     */
    public Optional<TextLineReference> selectTextLineAt(double x, double y, double epsilon) {
        List<TextLineReference> textLines = selectTextLinesAt(x, y, epsilon);
        return textLines.isEmpty() ? Optional.empty() : Optional.of(textLines.get(0));
    }

    public List<ImageReference> selectImages() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        return root.toImageObject(images);
    }

    public List<ImageReference> selectImagesAt(double x, double y) { return selectImagesAt(x, y, PDFDancer.DEFAULT_EPSILON); }

    public List<ImageReference> selectImagesAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        List<ObjectRef> filtered = images.stream()
                .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                .collect(Collectors.toUnmodifiableList());
        return root.toImageObject(filtered);
    }

    /**
     * Selects a single image at the specified coordinates with default epsilon.
     * @return Optional containing the first image found at the position, or empty if none found
     */
    public Optional<ImageReference> selectImageAt(double x, double y) {
        return selectImageAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    /**
     * Selects a single image at the specified coordinates with custom epsilon tolerance.
     * @return Optional containing the first image found at the position, or empty if none found
     */
    public Optional<ImageReference> selectImageAt(double x, double y, double epsilon) {
        List<ImageReference> images = selectImagesAt(x, y, epsilon);
        return images.isEmpty() ? Optional.empty() : Optional.of(images.get(0));
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

    public List<FormXObjectReference> selectFormsAt(double x, double y) { return selectFormsAt(x, y, PDFDancer.DEFAULT_EPSILON); }

    public List<FormXObjectReference> selectFormsAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageIndex, null);
        List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
        List<ObjectRef> filtered = forms.stream()
                .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                .collect(Collectors.toUnmodifiableList());
        return root.toFormXObject(filtered);
    }

    /**
     * Selects a single form XObject at the specified coordinates with default epsilon.
     * @return Optional containing the first form found at the position, or empty if none found
     */
    public Optional<FormXObjectReference> selectFormAt(double x, double y) {
        return selectFormAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    /**
     * Selects a single form XObject at the specified coordinates with custom epsilon tolerance.
     * @return Optional containing the first form found at the position, or empty if none found
     */
    public Optional<FormXObjectReference> selectFormAt(double x, double y, double epsilon) {
        List<FormXObjectReference> forms = selectFormsAt(x, y, epsilon);
        return forms.isEmpty() ? Optional.empty() : Optional.of(forms.get(0));
    }

    public List<FormFieldReference> selectFormFields() {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageIndex);
        return root.toFormFieldObject(formFields);
    }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y) { return selectFormFieldsAt(x, y, PDFDancer.DEFAULT_EPSILON); }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y, double epsilon) {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageIndex);
        return root.toFormFieldObject(
                formFields.stream()
                        .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Selects a single form field at the specified coordinates with default epsilon.
     * @return Optional containing the first form field found at the position, or empty if none found
     */
    public Optional<FormFieldReference> selectFormFieldAt(double x, double y) {
        return selectFormFieldAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    /**
     * Selects a single form field at the specified coordinates with custom epsilon tolerance.
     * @return Optional containing the first form field found at the position, or empty if none found
     */
    public Optional<FormFieldReference> selectFormFieldAt(double x, double y, double epsilon) {
        List<FormFieldReference> formFields = selectFormFieldsAt(x, y, epsilon);
        return formFields.isEmpty() ? Optional.empty() : Optional.of(formFields.get(0));
    }

    public List<TextParagraphReference> selectTextStartingWith(String text) { return this.selectParagraphsStartingWith(text); }

    public BezierBuilder newBezier() { return new BezierBuilder(root, pageIndex); }

    public PathBuilder newPath() { return new PathBuilder(root, pageIndex); }

    public LineBuilder newLine() { return new LineBuilder(root, pageIndex); }

    /**
     * Deletes the current page from the PDF document.
     * This method removes the page at the current pageIndex from the document permanently,
     * updating the page numbering for subsequent pages.
     *
     * @return true if the page was successfully deleted, false otherwise
     */
    public boolean delete() {
        ObjectRef pageRef = root.getPage(pageIndex);
        if (pageRef == null) {
            return false;
        }
        return Boolean.TRUE.equals(root.deletePage(pageRef));
    }
}
