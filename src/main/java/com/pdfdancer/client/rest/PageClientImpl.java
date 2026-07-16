package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
import com.pdfdancer.common.model.PathGroupInfo;
import com.pdfdancer.common.request.CreatePathGroupRequest;
import com.pdfdancer.common.response.PageSnapshot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Top-level implementation of page-scoped operations extracted from PDFDancer.PageClient.
 * The nested PDFDancer.PageClient will extend this class to preserve the public API type.
 */
class PageClientImpl {
    protected final PDFDancer root;
    protected int pageNumber;

    PageClientImpl(PDFDancer root, int pageNumber) {
        this.root = root;
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() { return pageNumber; }

    public List<PathReference> selectPathsAt(double x, double y) {
        Position position = new PositionBuilder().onPage(pageNumber).atCoordinates(x, y).build();
        return root.toPathObject(root.find(ObjectType.PATH, position));
    }

    public List<PathReference> selectPathsAt(double x, double y, double epsilon) {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
        List<ObjectRef> paths = root.collectObjectsByType(snapshot, Set.of(ObjectType.PATH));
        return root.toPathObject(paths.stream()
                .filter(ref -> root.containsPoint(ref, x, y, epsilon))
                .collect(Collectors.toUnmodifiableList()));
    }

    /**
     * Selects a single path at the specified coordinates.
     * @return Optional containing the first path found at the position, or empty if none found
     */
    public Optional<PathReference> selectPathAt(double x, double y) {
        List<PathReference> paths = selectPathsAt(x, y);
        return paths.isEmpty() ? Optional.empty() : Optional.of(paths.get(0));
    }

    public Optional<PathReference> selectPathAt(double x, double y, double epsilon) {
        List<PathReference> paths = selectPathsAt(x, y, epsilon);
        return paths.isEmpty() ? Optional.empty() : Optional.of(paths.get(0));
    }

    public Optional<PathReference> selectPath() {
        List<PathReference> paths = selectPaths();
        return paths.isEmpty() ? Optional.empty() : Optional.of(paths.get(0));
    }

    public List<ImageReference> selectImages() {
        PageSnapshot snapshot = root.getPageSnapshotCached(pageNumber, null);
        List<ObjectRef> images = root.collectObjectsByType(snapshot, Set.of(ObjectType.IMAGE));
        return root.toImageObject(images);
    }

    public List<ImageReference> selectImagesAt(double x, double y) { return selectImagesAt(x, y, PDFDancer.DEFAULT_EPSILON); }

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

    public Optional<ImageReference> selectImage() {
        List<ImageReference> images = selectImages();
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

    public PathGroupReference groupPaths(List<String> pathIds) {
        int pageIndex = pageNumber - 1;
        CreatePathGroupRequest request = new CreatePathGroupRequest(pageIndex, pathIds, null);
        PathGroupInfo info = root.createPathGroup(request);
        return new PathGroupReference(root, info, pageNumber);
    }

    public PathGroupReference groupPathsInRegion(BoundingRect region) {
        int pageIndex = pageNumber - 1;
        CreatePathGroupRequest request = new CreatePathGroupRequest(pageIndex, null, region);
        PathGroupInfo info = root.createPathGroup(request);
        return new PathGroupReference(root, info, pageNumber);
    }

    public List<PathGroupReference> getPathGroups() {
        return root.getPathGroups(pageNumber);
    }

    public List<FormXObjectReference> selectFormsAt(double x, double y) { return selectFormsAt(x, y, PDFDancer.DEFAULT_EPSILON); }

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

    public Optional<FormXObjectReference> selectForm() {
        List<FormXObjectReference> forms = selectForms();
        return forms.isEmpty() ? Optional.empty() : Optional.of(forms.get(0));
    }

    public List<FormFieldReference> selectFormFields() {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageNumber);
        return root.toFormFieldObject(formFields);
    }

    public List<FormFieldReference> selectFormFieldsAt(double x, double y) { return selectFormFieldsAt(x, y, PDFDancer.DEFAULT_EPSILON); }

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

    public Optional<FormFieldReference> selectFormField() {
        List<FormFieldReference> fields = selectFormFields();
        return fields.isEmpty() ? Optional.empty() : Optional.of(fields.get(0));
    }

    /**
     * Selects all form fields on this page with the specified name.
     * @param name the name of the form fields to find
     * @return list of form fields with the given name on this page
     */
    public List<FormFieldReference> selectFormFieldsByName(String name) {
        List<FormFieldRef> formFields = root.collectFormFieldRefsFromPage(pageNumber);
        return root.toFormFieldObject(
                formFields.stream()
                        .filter(ref -> Objects.equals(ref.getName(), name))
                        .collect(Collectors.toUnmodifiableList())
        );
    }

    /**
     * Selects a single form field on this page with the specified name.
     * @param name the name of the form field to find
     * @return Optional containing the first form field with the given name, or empty if none found
     */
    public Optional<FormFieldReference> selectFormFieldByName(String name) {
        List<FormFieldReference> formFields = selectFormFieldsByName(name);
        return formFields.isEmpty() ? Optional.empty() : Optional.of(formFields.get(0));
    }

    public BezierBuilder newBezier() { return new BezierBuilder(root, pageNumber); }

    public PathBuilder newPath() { return new PathBuilder(root, pageNumber); }

    public LineBuilder newLine() { return new LineBuilder(root, pageNumber); }

    public RectangleBuilder newRectangle() { return new RectangleBuilder(root, pageNumber); }

    public ImageBuilder newImage() { return new ImageBuilder(root, pageNumber); }

    public PageSnapshot getSnapshot() { return root.getPageSnapshot(pageNumber); }

    public PageSnapshot getSnapshot(String types) { return root.getPageSnapshot(pageNumber, types); }

    public List<ObjectRef> selectElements() { return getSnapshot().elements(); }

    public List<ObjectRef> selectElements(String types) { return getSnapshot(types).elements(); }

    public boolean moveTo(int targetPageNumber) {
        boolean moved = root.movePage(pageNumber, targetPageNumber);
        if (moved) pageNumber = targetPageNumber;
        return moved;
    }

    public PageTextClient text() {
        return new PageTextClient(root, pageNumber);
    }

    /**
     * Deletes the current page from the PDF document.
     * This method removes the page at the current pageNumber from the document permanently,
     * updating the page numbering for subsequent pages.
     *
     * @return true if the page was successfully deleted, false otherwise
     */
    public boolean delete() {
        ObjectRef pageRef = root.getPage(pageNumber);
        if (pageRef == null) {
            return false;
        }
        Boolean result = root.deletePage(pageRef);
        return result != null && result;
    }
}
