package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
import com.pdfdancer.common.response.PageSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Page-scoped operations extracted from PDFDancer.PageClient.
 * PDFDancer.PageClient becomes a thin wrapper extending this class to preserve API.
 */
public class PageApi {
    protected final PDFDancer root;
    protected final int pageNumber;

    public PageApi(PDFDancer root, int pageNumber) {
        this.root = root;
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public List<PathReference> selectPathsAt(double x, double y) {
        Position position = new PositionBuilder().onPage(pageNumber).atCoordinates(x, y).build();
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

    public List<ImageReference> selectImages() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
        List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        return root.toImageObject(images);
    }

    public List<ImageReference> selectImagesAt(double x, double y) {
        return selectImagesAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<ImageReference> selectImagesAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
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
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
        List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.FORM_X_OBJECT));
        return root.toFormXObject(forms);
    }

    public List<PathReference> selectPaths() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
        List<ObjectRef> forms = root.collectObjectsByType(snapshot, Set.of(ObjectType.PATH));
        return root.toPathObject(forms);
    }

    public List<FormXObjectReference> selectFormsAt(double x, double y) {
        return selectFormsAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<FormXObjectReference> selectFormsAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
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
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageNumber);
        return root.toFormFieldObject(formFields);
    }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y) {
        return selectFormFieldsAt(x, y, PDFDancer.DEFAULT_EPSILON);
    }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y, double epsilon) {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageNumber);
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

    public BezierBuilder newBezier() {
        return new BezierBuilder(root, pageNumber);
    }

    public PathBuilder newPath() {
        return new PathBuilder(root, pageNumber);
    }

    public LineBuilder newLine() {
        return new LineBuilder(root, pageNumber);
    }

    public PageTextClient text() {
        return new PageTextClient(root, pageNumber);
    }
}
