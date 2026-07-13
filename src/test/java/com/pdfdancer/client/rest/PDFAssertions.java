package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageRef;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.pdfdancer.client.rest.BaseTest.getValidToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fluent assertion helper for PDF validation.
 * Matches the Python PDFAssertions API for consistent testing across clients.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class PDFAssertions {

    private final PDFDancer pdf;
    private final String token;
    private final PdfDancerHttpClient httpClient;
    private final File savedPdfFile;
    private final PdfDrawInspector drawInspector;

    public PDFAssertions(PDFDancer pdfDancer) {
        this(pdfDancer, getValidToken(), pdfDancer.getHttpClient());
    }

    public PDFAssertions(PDFDancer pdfDancer, String token, PdfDancerHttpClient httpClient) {
        this.token = token;
        this.httpClient = httpClient;

        // Save and reload to get fresh state (matches Python behavior)
        try {
            File tempFile = File.createTempFile("client-assertions-", ".pdf");
            pdfDancer.save(tempFile.getAbsolutePath());
            System.out.println("Saved client file to " + tempFile.getAbsolutePath());

            // Reload from the saved file
            byte[] pdfBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
            this.pdf = PDFDancer.createSession(token, pdfBytes, httpClient);
            this.savedPdfFile = tempFile;
            this.drawInspector = new PdfDrawInspector(this.pdf, tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PDFAssertions", e);
        }
    }

    public void assertElementMatches(Predicate<? super ObjectRef> predicate, int pageIndex) {
        boolean found = pdf.getPageSnapshot(pageIndex).elements().stream().anyMatch(predicate);
        assertTrue(found, "Element not found");
    }

    // ===========================
    // Page Assertions
    // ===========================

    public PDFAssertions assertNumberOfPages(int pageCount) {
        assertEquals(pageCount, pdf.getPages().size(),
                String.format("Expected %d pages, but got %d", pageCount, pdf.getPages().size()));
        return this;
    }

    public PDFAssertions assertPageCount(int pageCount) {
        assertEquals(pageCount, pdf.getPages().size());
        return this;
    }

    public PDFAssertions assertPageDimension(double width, double height, int pageNumber) {
        return assertPageDimension(width, height, null, pageNumber);
    }

    public PDFAssertions assertPageDimension(double width, double height, Orientation orientation, int pageNumber) {
        PageRef page = pdf.getPages().get(pageNumber - 1);
        assertEquals(width, page.getPageSize().getWidth(), 0.001,
                String.format("%f != %f", width, page.getPageSize().getWidth()));
        assertEquals(height, page.getPageSize().getHeight(), 0.001,
                String.format("%f != %f", height, page.getPageSize().getHeight()));

        if (orientation != null) {
            Orientation actualOrientation = page.getOrientation();
            assertEquals(orientation, actualOrientation,
                    String.format("%s != %s", orientation, actualOrientation));
        }

        return this;
    }

    // ===========================
    // Path Assertions
    // ===========================

    public PDFAssertions assertPathIsAt(String internalId, double x, double y, int page) {
        return assertPathIsAt(internalId, x, y, page, 1e-6);
    }

    public PDFAssertions assertPathIsAt(String internalId, double x, double y, int page, double epsilon) {
        List<PathReference> paths = pdf.page(page).selectPathsAt(x, y);
        assertEquals(1, paths.size());

        PathReference ref = paths.get(0);
        assertEquals(internalId, ref.getInternalId(),
                String.format("%s != %s", internalId, ref.getInternalId()));
        assertEquals(x, ref.getPosition().getX(), epsilon,
                String.format("%f != %f", x, ref.getPosition().getX()));
        assertEquals(y, ref.getPosition().getY(), epsilon,
                String.format("%f != %f", y, ref.getPosition().getY()));

        return this;
    }

    public PDFAssertions assertNoPathAt(double x, double y, int page) {
        List<PathReference> paths = pdf.page(page).selectPathsAt(x, y);
        assertEquals(0, paths.size());
        return this;
    }

    public PDFDancer getPdf() {
        return pdf;
    }

    public PDFAssertions assertPathHasBounds(String internalId, double expectedWidth, double expectedHeight, int page) {
        return assertPathHasBounds(internalId, expectedWidth, expectedHeight, page, 1.0);
    }

    public PDFAssertions assertPathHasBounds(String internalId, double expectedWidth, double expectedHeight, int page, double epsilon) {
        List<PathReference> paths = pdf.page(page).selectPaths();
        PathReference ref = paths.stream()
                .filter(p -> internalId.equals(p.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Path with ID " + internalId + " not found on page " + page));

        com.pdfdancer.common.model.BoundingRect bounds = ref.getPosition().getBoundingRect();
        assertNotNull(bounds, "Path " + internalId + " has no bounding rect");
        assertEquals(expectedWidth, bounds.getWidth(), epsilon,
                String.format("Path %s width: expected %f but got %f", internalId, expectedWidth, bounds.getWidth()));
        assertEquals(expectedHeight, bounds.getHeight(), epsilon,
                String.format("Path %s height: expected %f but got %f", internalId, expectedHeight, bounds.getHeight()));

        return this;
    }

    public PDFAssertions assertPathHasClipping(String internalId) {
        return assertPathHasClipping(internalId, 1);
    }

    public PDFAssertions assertPathHasClipping(String internalId, int page) {
        assertTrue(drawInspector.pathHasClipping(internalId, page),
                "Expected path " + internalId + " to still be clipped");
        return this;
    }

    public PDFAssertions assertPathHasNoClipping(String internalId) {
        return assertPathHasNoClipping(internalId, 1);
    }

    public PDFAssertions assertPathHasNoClipping(String internalId, int page) {
        assertFalse(drawInspector.pathHasClipping(internalId, page),
                "Expected path " + internalId + " to have clipping removed");
        return this;
    }

    public PDFAssertions assertNumberOfPaths(int pathCount, int page) {
        List<PathReference> paths = pdf.selectPaths();
        // Filter to specific page
        long pagePathCount = paths.stream()
                .filter(p -> p.getPosition().getPageNumber() == page)
                .count();
        assertEquals(pathCount, pagePathCount,
                String.format("Expected %d paths, but got %d", pathCount, pagePathCount));
        return this;
    }

    public PDFAssertions assertPathHasStrokeColor(String internalId, Color expectedColor, int page) {
        return assertPathHasStrokeColor(internalId, expectedColor, page, 1e-6);
    }

    public PDFAssertions assertPathHasStrokeColor(String internalId, Color expectedColor, int page, double epsilon) {
        List<PathReference> paths = pdf.page(page).selectPaths();
        PathReference ref = paths.stream()
                .filter(p -> internalId.equals(p.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Path with ID " + internalId + " not found on page " + page));

        Color actualColor = ref.getStrokeColor();
        assertNotNull(actualColor, "Path " + internalId + " stroke color should not be null");
        assertEquals(expectedColor.getRed(), actualColor.getRed(), epsilon,
                String.format("Path %s stroke color red: expected %d but got %d", internalId, expectedColor.getRed(), actualColor.getRed()));
        assertEquals(expectedColor.getGreen(), actualColor.getGreen(), epsilon,
                String.format("Path %s stroke color green: expected %d but got %d", internalId, expectedColor.getGreen(), actualColor.getGreen()));
        assertEquals(expectedColor.getBlue(), actualColor.getBlue(), epsilon,
                String.format("Path %s stroke color blue: expected %d but got %d", internalId, expectedColor.getBlue(), actualColor.getBlue()));
        assertEquals(expectedColor.getAlpha(), actualColor.getAlpha(), epsilon,
                String.format("Path %s stroke color alpha: expected %d but got %d", internalId, expectedColor.getAlpha(), actualColor.getAlpha()));

        return this;
    }

    public PDFAssertions assertPathHasFillColor(String internalId, Color expectedColor, int page) {
        return assertPathHasFillColor(internalId, expectedColor, page, 1e-6);
    }

    public PDFAssertions assertPathHasFillColor(String internalId, Color expectedColor, int page, double epsilon) {
        List<PathReference> paths = pdf.page(page).selectPaths();
        PathReference ref = paths.stream()
                .filter(p -> internalId.equals(p.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Path with ID " + internalId + " not found on page " + page));

        Color actualColor = ref.getFillColor();
        assertNotNull(actualColor, "Path " + internalId + " fill color should not be null");
        assertEquals(expectedColor.getRed(), actualColor.getRed(), epsilon,
                String.format("Path %s fill color red: expected %d but got %d", internalId, expectedColor.getRed(), actualColor.getRed()));
        assertEquals(expectedColor.getGreen(), actualColor.getGreen(), epsilon,
                String.format("Path %s fill color green: expected %d but got %d", internalId, expectedColor.getGreen(), actualColor.getGreen()));
        assertEquals(expectedColor.getBlue(), actualColor.getBlue(), epsilon,
                String.format("Path %s fill color blue: expected %d but got %d", internalId, expectedColor.getBlue(), actualColor.getBlue()));
        assertEquals(expectedColor.getAlpha(), actualColor.getAlpha(), epsilon,
                String.format("Path %s fill color alpha: expected %d but got %d", internalId, expectedColor.getAlpha(), actualColor.getAlpha()));

        return this;
    }

    // ===========================
    // PDF Text Content Assertions
    // ===========================

    public PDFAssertions assertPdfTextContains(String text) {
        assertTrue(extractPdfText().contains(text),
                String.format("Expected saved PDF text to contain '%s'", text));
        return this;
    }

    public PDFAssertions assertPdfTextContains(String text, int page) {
        assertTrue(extractPdfText(page, page).contains(text),
                String.format("Expected saved PDF page %d text to contain '%s'", page, text));
        return this;
    }

    public PDFAssertions assertPdfTextDoesNotContain(String text) {
        assertFalse(extractPdfText().contains(text),
                String.format("Expected saved PDF text not to contain '%s'", text));
        return this;
    }

    public PDFAssertions assertPdfTextDoesNotContain(String text, int page) {
        assertFalse(extractPdfText(page, page).contains(text),
                String.format("Expected saved PDF page %d text not to contain '%s'", page, text));
        return this;
    }

    public PDFAssertions assertPdfTextOccurrenceCount(String text, int expectedCount) {
        assertEquals(expectedCount, countOccurrences(extractPdfText(), text),
                String.format("Expected saved PDF text to contain '%s' %d times", text, expectedCount));
        return this;
    }

    public PDFAssertions assertPdfTextOccurrenceCount(String text, int expectedCount, int page) {
        assertEquals(expectedCount, countOccurrences(extractPdfText(page, page), text),
                String.format("Expected saved PDF page %d text to contain '%s' %d times", page, text, expectedCount));
        return this;
    }

    private String extractPdfText() {
        return extractPdfText(1, Integer.MAX_VALUE);
    }

    private String extractPdfText(int startPage, int endPage) {
        try (PDDocument document = Loader.loadPDF(savedPdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from saved PDF", e);
        }
    }

    private int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    // ===========================
    // Image Assertions
    // ===========================

    public PDFAssertions assertNumberOfImages(int imageCount, int page) {
        List<ImageReference> images = pdf.page(page).selectImages();
        assertEquals(imageCount, images.size(),
                String.format("Expected %d image but got %d", imageCount, images.size()));
        return this;
    }

    public PDFAssertions assertImageAt(double x, double y, int page) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y);
        List<ImageReference> allImages = pdf.page(page).selectImages();
        assertEquals(1, images.size(),
                String.format("Expected 1 image but got %d, total images: %d, first pos: %s",
                        images.size(), allImages.size(),
                        allImages.isEmpty() ? "none" : allImages.get(0).getPosition()));
        return this;
    }

    public PDFAssertions assertNoImageAt(double x, double y, int page) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y);
        assertEquals(0, images.size(),
                String.format("Expected 0 image at %f/%f but got %d, %s",
                        x, y, images.size(),
                        images.isEmpty() ? "" : images.get(0).getInternalId()));
        return this;
    }

    public PDFAssertions assertImageWithIdAt(String internalId, double x, double y, int page) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y);
        assertEquals(1, images.size(),
                String.format("Expected 1 image but got %d", images.size()));
        assertEquals(internalId, images.get(0).getInternalId(),
                String.format("%s != %s", internalId, images.get(0).getInternalId()));
        return this;
    }

    public PDFAssertions assertImageSize(double x, double y, int page, double expectedWidth, double expectedHeight, double epsilon) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y, epsilon);
        assertEquals(1, images.size(),
                String.format("Expected 1 image at %f/%f but got %d", x, y, images.size()));
        ImageReference image = images.get(0);
        assertEquals(expectedWidth, image.getWidth(), epsilon,
                String.format("Image width: expected %f but got %f", expectedWidth, image.getWidth()));
        assertEquals(expectedHeight, image.getHeight(), epsilon,
                String.format("Image height: expected %f but got %f", expectedHeight, image.getHeight()));
        return this;
    }

    public PDFAssertions assertImageSize(double x, double y, int page, double expectedWidth, double expectedHeight) {
        return assertImageSize(x, y, page, expectedWidth, expectedHeight, 1.0);
    }

    public PDFAssertions assertImageWidthChanged(double x, double y, int page, double originalWidth, double epsilon) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y, epsilon);
        assertEquals(1, images.size(),
                String.format("Expected 1 image at %f/%f but got %d", x, y, images.size()));
        ImageReference image = images.get(0);
        assertTrue(Math.abs(image.getWidth() - originalWidth) > epsilon,
                String.format("Image width should have changed from %f but is still %f", originalWidth, image.getWidth()));
        return this;
    }

    public PDFAssertions assertImageHeightChanged(double x, double y, int page, double originalHeight, double epsilon) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y, epsilon);
        assertEquals(1, images.size(),
                String.format("Expected 1 image at %f/%f but got %d", x, y, images.size()));
        ImageReference image = images.get(0);
        assertTrue(Math.abs(image.getHeight() - originalHeight) > epsilon,
                String.format("Image height should have changed from %f but is still %f", originalHeight, image.getHeight()));
        return this;
    }

    public PDFAssertions assertImageAspectRatio(double x, double y, int page, double expectedAspectRatio, double epsilon) {
        List<ImageReference> images = pdf.page(page).selectImagesAt(x, y, epsilon);
        assertEquals(1, images.size(),
                String.format("Expected 1 image at %f/%f but got %d", x, y, images.size()));
        ImageReference image = images.get(0);
        Double actualRatio = image.getAspectRatio();
        assertNotNull(actualRatio, "Image aspect ratio should not be null");
        assertEquals(expectedAspectRatio, actualRatio, epsilon,
                String.format("Image aspect ratio: expected %f but got %f", expectedAspectRatio, actualRatio));
        return this;
    }

    public PDFAssertions assertImageAspectRatioPreserved(double x, double y, int page, double originalAspectRatio, double epsilon) {
        return assertImageAspectRatio(x, y, page, originalAspectRatio, epsilon);
    }

    public PDFAssertions assertFirstImageOnPage(int page, java.util.function.Consumer<ImageReference> assertion) {
        List<ImageReference> images = pdf.page(page).selectImages();
        assertTrue(images.size() >= 1, "Expected at least 1 image on page " + page);
        assertion.accept(images.get(0));
        return this;
    }

    public PDFAssertions assertImageHasClipping(String internalId) {
        return assertImageHasClipping(internalId, 1);
    }

    public PDFAssertions assertImageHasClipping(String internalId, int page) {
        assertTrue(drawInspector.imageHasClipping(internalId, page),
                "Expected image " + internalId + " to still be clipped");
        return this;
    }

    public PDFAssertions assertImageHasNoClipping(String internalId) {
        return assertImageHasNoClipping(internalId, 1);
    }

    public PDFAssertions assertImageHasNoClipping(String internalId, int page) {
        assertFalse(drawInspector.imageHasClipping(internalId, page),
                "Expected image " + internalId + " to have clipping removed");
        return this;
    }

    // ===========================
    // Element Count Assertions
    // ===========================

    public PDFAssertions assertTotalNumberOfElements(int nrOfElements) {
        int total = 0;
        for (PageRef pageRef : pdf.getPages()) {
            int pageNumber = pageRef.getPosition().getPageNumber();
            total += getNumberOfElements(pageNumber);
        }
        assertEquals(nrOfElements, total,
                String.format("Total number of elements differ, actual %d != expected %d", total, nrOfElements));
        return this;
    }

    public PDFAssertions assertTotalNumberOfElements(int nrOfElements, int pageNumber) {
        int total = getNumberOfElements(pageNumber);
        assertEquals(nrOfElements, total,
                String.format("Total number of elements differ, actual %d != expected %d", total, nrOfElements));
        return this;
    }

    private int getNumberOfElements(int pageNumber) {
        int total = 0;
        total += pdf.page(pageNumber).selectFormFields().size();
        total += pdf.page(pageNumber).selectForms().size();
        total += pdf.page(pageNumber).selectImages().size();
        total += pdf.page(pageNumber).selectPaths().size();
        return total;
    }

    // ===========================
    // Form Assertions
    // ===========================

    public PDFAssertions assertNumberOfFormxobjects(int nrOfFormxobjects, int pageNumber) {
        assertEquals(nrOfFormxobjects, pdf.page(pageNumber).selectForms().size(),
                String.format("Expected nr of formxobjects %d but got %d",
                        nrOfFormxobjects, pdf.page(pageNumber).selectForms().size()));
        return this;
    }

    public PDFAssertions assertNumberOfFormFields(int nrOfFormFields, int pageNumber) {
        assertEquals(nrOfFormFields, pdf.page(pageNumber).selectFormFields().size(),
                String.format("Expected nr of form fields %d but got %d",
                        nrOfFormFields, pdf.page(pageNumber).selectFormFields().size()));
        return this;
    }

    public PDFAssertions assertFormFieldAt(double x, double y, int page) {
        List<FormFieldReference> formFields = pdf.page(page).selectFormFieldsAt(x, y);
        List<FormFieldReference> allFormFields = pdf.page(page).selectFormFields();
        assertEquals(1, formFields.size(),
                String.format("Expected 1 form field but got %d, total form_fields: %d, first pos: %s",
                        formFields.size(), allFormFields.size(),
                        allFormFields.isEmpty() ? "none" : allFormFields.get(0).getPosition()));
        return this;
    }

    public PDFAssertions assertFormFieldNotAt(double x, double y, int page) {
        List<FormFieldReference> formFields = pdf.page(page).selectFormFieldsAt(x, y);
        assertEquals(0, formFields.size(),
                String.format("Expected 0 form fields at %f/%f but got %d, %s",
                        x, y, formFields.size(),
                        formFields.isEmpty() ? "" : formFields.get(0).getInternalId()));
        return this;
    }

    public PDFAssertions assertFormFieldExists(String fieldName, int pageNumber) {
        List<FormFieldReference> formFields = pdf.selectFormFieldsByName(fieldName);
        // Filter to page
        long pageFormFields = formFields.stream()
                .filter(f -> f.getPosition().getPageNumber() == pageNumber)
                .count();
        assertEquals(1, pageFormFields,
                String.format("Expected 1 form field but got %d", pageFormFields));
        return this;
    }

    public PDFAssertions assertFormFieldHasValue(String fieldName, String fieldValue, int pageNumber) {
        List<FormFieldReference> formFields = pdf.selectFormFieldsByName(fieldName);
        // Filter to page
        List<FormFieldReference> pageFormFields = formFields.stream()
                .filter(f -> f.getPosition().getPageNumber() == pageNumber)
                .collect(Collectors.toUnmodifiableList());
        assertEquals(1, pageFormFields.size(),
                String.format("Expected 1 form field but got %d", pageFormFields.size()));
        assertEquals(fieldValue, pageFormFields.get(0).getValue(),
                String.format("%s != %s", pageFormFields.get(0).getValue(), fieldValue));
        return this;
    }

}
