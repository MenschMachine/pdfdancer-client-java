package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageRef;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.pdfdancer.client.rest.BaseTest.getValidToken;
import static org.junit.jupiter.api.Assertions.*;

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

    public PDFAssertions(PDFDancer pdfDancer) {
        this(pdfDancer, getValidToken(), pdfDancer.getHttpClient());
    }

    public PDFAssertions(PDFDancer pdfDancer, String token, PdfDancerHttpClient httpClient) {
        this.token = token;
        this.httpClient = httpClient;

        // Save and reload to get fresh state (matches Python behavior)
        try {
            File tempFile = File.createTempFile("client-assertions-", ".pdf");
            tempFile.deleteOnExit();
            pdfDancer.save(tempFile.getAbsolutePath());
            System.out.println("Saved client file to " + tempFile.getAbsolutePath());
            this.savedPdfFile = tempFile;

            // Reload from the saved file
            byte[] pdfBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
            this.pdf = PDFDancer.createSession(token, pdfBytes, httpClient);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PDFAssertions", e);
        }
    }

    public void assertElementMatches(Predicate<? super ObjectRef> predicate, int pageIndex) {
        boolean found = pdf.getPageSnapshot(pageIndex).elements().stream().anyMatch(predicate);
        assertTrue(found, "Element not found");
    }

    // ===========================
    // Text Assertions
    // ===========================

    public PDFAssertions assertTextHasColor(String text, Color color, int page) {
        assertTextlineHasColor(text, color, page);

        return withParagraphDump(page, () -> {
            List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(text);
            assertEquals(1, paragraphs.size(),
                    String.format("Expected 1 paragraph but got %d", paragraphs.size()));

            TextParagraphReference ref = paragraphs.get(0);
            assertTrue(ref.getText().contains(text));
            assertEquals(color, ref.getColor(),
                    String.format("%s != %s", color, ref.getColor()));

            return this;
        });
    }

    public PDFAssertions assertTextHasFont(String text, String fontName, double fontSize, int page) {
        assertTextlineHasFont(text, fontName, fontSize, page);

        return withParagraphDump(page, () -> {
            List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
            assertEquals(1, paragraphs.size(),
                    String.format("Expected 1 paragraph but got %d", paragraphs.size()));

            TextParagraphReference ref = paragraphs.get(0);
            assertEquals(fontName, ref.getFontName(),
                    String.format("Expected %s to match %s", ref.getFontName(), fontName));
            assertEquals(fontSize, ref.getFontSize(), 0.001);

            return this;
        });
    }

    public PDFAssertions assertParagraphIsAt(String text, double x, double y, int page) {
        return assertParagraphIsAt(text, x, y, page, 2d); // adjust for baseline vs. bounding box differences
    }

    public PDFAssertions assertParagraphIsAt(String text, double x, double y, int page, double epsilon) {
        List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
        assertEquals(1, paragraphs.size(),
                String.format("Expected 1 paragraph but got %d", paragraphs.size()));

        TextParagraphReference ref = paragraphs.get(0);
        assertEquals(x, ref.getPosition().getX(), epsilon,
                String.format("%f != %f", x, ref.getPosition().getX()));
        assertEquals(y, ref.getPosition().getY(), epsilon,
                String.format("%f != %f", y, ref.getPosition().getY()));

        List<TextParagraphReference> byPosition = pdf.page(page).selectParagraphsAt(x, y, epsilon);
        assertEquals(1, byPosition.size(),
                String.format("Expected 1 paragraph but got %d", byPosition.size()));
        assertEquals(paragraphs.get(0).getInternalId(), byPosition.get(0).getInternalId());

        return this;
    }

    public PDFAssertions assertTextHasFontMatching(String text, String fontName, double fontSize, int page) {
        assertTextlineHasFontMatching(text, fontName, fontSize, page);

        List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
        assertEquals(1, paragraphs.size(),
                String.format("Expected 1 paragraph but got %d", paragraphs.size()));

        TextParagraphReference ref = paragraphs.get(0);
        assertTrue(ref.getFontName().contains(fontName),
                String.format("Expected %s to match %s", ref.getFontName(), fontName));
        assertEquals(fontSize, ref.getFontSize(), 0.001);

        return this;
    }

    // ===========================
    // Text Line Assertions
    // ===========================

    public PDFAssertions assertTextlineHasColor(String text, Color color, int page) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
            assertEquals(1, lines.size(),
                    String.format("Expected 1 line but got %d", lines.size()));

            TextLineReference ref = lines.get(0);
            assertEquals(color, ref.getColor(),
                    String.format("%s != %s", color, ref.getColor()));
            assertTrue(ref.getText().contains(text));

            return this;
        });
    }

    public PDFAssertions assertTextlineHasFont(String text, String fontName, double fontSize, int page) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
            assertEquals(1, lines.size(),
                    String.format("Expected 1 line but got %d", lines.size()));

            TextLineReference ref = lines.get(0);
            assertEquals(fontName, ref.getFontName(),
                    String.format("Expected %s but got %s", fontName, ref.getFontName()));
            assertEquals(fontSize, ref.getFontSize(), 0.001,
                    String.format("%f != %f", fontSize, ref.getFontSize()));

            return this;
        });
    }

    public PDFAssertions assertTextlineHasFontMatching(String text, String fontName, double fontSize, int page) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
            assertEquals(1, lines.size(),
                    String.format("Expected 1 line but got %d", lines.size()));

            TextLineReference ref = lines.get(0);
            assertTrue(ref.getFontName().contains(fontName),
                    String.format("Expected %s to match %s", ref.getFontName(), fontName));
            assertEquals(fontSize, ref.getFontSize(), 0.001);

            return this;
        });
    }

    public PDFAssertions assertTextlineIsAt(String text, double x, double y, int page) {
        return assertTextlineIsAt(text, x, y, page, 1e-6);
    }

    public PDFAssertions assertTextlineIsAt(String text, double x, double y, int page, double epsilon) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
            assertEquals(1, lines.size());

            TextLineReference ref = lines.get(0);
            assertEquals(x, ref.getPosition().getX(), epsilon,
                    String.format("%f != %f", x, ref.getPosition().getX()));
            assertEquals(y, ref.getPosition().getY(), epsilon,
                    String.format("%f != %f", y, ref.getPosition().getY()));

            List<TextLineReference> byPosition = pdf.page(page).selectTextLinesAt(x, y, epsilon);
            assertEquals(1, byPosition.size());
            assertEquals(lines.get(0).getInternalId(), byPosition.get(0).getInternalId());

            return this;
        });
    }

    public PDFAssertions assertTextlineDoesNotExist(String text, int page) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
            assertEquals(0, lines.size());
            return this;
        });
    }

    public PDFAssertions assertTextlineExists(String pattern, int page) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesMatching(".*" + pattern + ".*");
            assert (!lines.isEmpty());
            return this;
        });
    }

    public PDFAssertions assertParagraphExists(String text, int page) {
        return withParagraphDump(page, () -> {
            List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
            assertEquals(1, paragraphs.size(),
                    String.format("No paragraphs starting with %s found on page %d", text, page));
            return this;
        });
    }

    public PDFAssertions assertParagraphNotExists(String text, int page) {
        return withParagraphDump(page, () -> {
            List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
            assertEquals(0, paragraphs.size(),
                    String.format("Paragraphs starting with %s found on page %d", text, page));
            return this;
        });
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

    public PDFAssertions assertPathHasClipping(String internalId, int page) {
        boolean clipped = findPathClippingState(internalId, page);
        assertTrue(clipped, String.format("Expected path %s on page %d to be clipped", internalId, page));
        return this;
    }

    public PDFAssertions assertPathHasNoClipping(String internalId, int page) {
        boolean clipped = findPathClippingState(internalId, page);
        assertFalse(clipped, String.format("Expected path %s on page %d to be unclipped", internalId, page));
        return this;
    }

    public PDFAssertions assertImageHasClipping(String internalId, int page) {
        boolean clipped = findImageClippingState(internalId, page);
        assertTrue(clipped, String.format("Expected image %s on page %d to be clipped", internalId, page));
        return this;
    }

    public PDFAssertions assertImageHasNoClipping(String internalId, int page) {
        boolean clipped = findImageClippingState(internalId, page);
        assertFalse(clipped, String.format("Expected image %s on page %d to be unclipped", internalId, page));
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
        total += pdf.page(pageNumber).selectParagraphs().size();
        total += pdf.page(pageNumber).selectFormFields().size();
        total += pdf.page(pageNumber).selectForms().size();
        total += pdf.page(pageNumber).selectImages().size();
        total += pdf.page(pageNumber).selectPaths().size();
        total += pdf.page(pageNumber).selectTextLines().size();
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

    public void assertParagraphHasColor(String text, Color color, int pageNumber) {
        TextParagraphReference paragraph = pdf.page(pageNumber).selectParagraphsStartingWith(text).get(0);
        assertEquals(color, paragraph.getColor(),
                String.format("%s != %s", color, paragraph.getColor()));
    }

    private boolean findPathClippingState(String internalId, int page) {
        PathReference pathRef = pdf.page(page).selectPaths().stream()
                .filter(path -> internalId.equals(path.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("Path with ID %s not found on page %d", internalId, page)));

        Double x = pathRef.getPosition().getX();
        Double y = pathRef.getPosition().getY();
        assertNotNull(x, "Path " + internalId + " has no x position");
        assertNotNull(y, "Path " + internalId + " has no y position");

        List<DrawEvent> events = extractPageDrawEvents(page).pathEvents();
        List<DrawEvent> matches = events.stream()
                .filter(event -> bboxContainsPoint(event.bbox(), x, y, 0.5))
                .collect(Collectors.toList());

        assertFalse(matches.isEmpty(),
                String.format(
                        "No draw event matched path anchor (%f, %f) for %s. Available path bboxes: %s",
                        x, y, internalId, events.stream().map(event -> bboxToString(event.bbox())).collect(Collectors.toList())));

        DrawEvent best = matches.stream()
                .min(Comparator.comparingDouble(event -> bboxArea(event.bbox())))
                .orElseThrow();
        return best.clipped();
    }

    private boolean findImageClippingState(String internalId, int page) {
        ImageReference imageRef = getImageById(internalId, page);
        com.pdfdancer.common.model.BoundingRect bbox = imageRef.getPosition().getBoundingRect();
        assertNotNull(bbox, "Image " + internalId + " has no bounding rect");

        double[] targetBBox = new double[]{
                bbox.getX(),
                bbox.getY(),
                bbox.getX() + bbox.getWidth(),
                bbox.getY() + bbox.getHeight()
        };

        List<DrawEvent> events = extractPageDrawEvents(page).imageEvents();
        List<DrawEvent> matches = events.stream()
                .filter(event -> bboxIntersectionArea(targetBBox, event.bbox()) > 0)
                .collect(Collectors.toList());

        assertFalse(matches.isEmpty(),
                String.format(
                        "No image draw event overlapped expected bbox %s for %s. Available image bboxes: %s",
                        bboxToString(targetBBox), internalId, events.stream().map(event -> bboxToString(event.bbox())).collect(Collectors.toList())));

        DrawEvent best = matches.stream()
                .max(Comparator.comparingDouble(event -> bboxIntersectionArea(targetBBox, event.bbox())))
                .orElseThrow();
        return best.clipped();
    }

    private ImageReference getImageById(String internalId, int page) {
        return pdf.page(page).selectImages().stream()
                .filter(image -> internalId.equals(image.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        String.format("Image with ID %s not found on page %d", internalId, page)));
    }

    private ParsedPageEvents extractPageDrawEvents(int page) {
        try (PDDocument document = Loader.loadPDF(savedPdfFile)) {
            assertTrue(page >= 1 && page <= document.getNumberOfPages(),
                    String.format("Page %d out of bounds for document with %d pages", page, document.getNumberOfPages()));

            PDPage pageObject = document.getPage(page - 1);
            PDFStreamParser parser = new PDFStreamParser(pageObject);
            List<Object> tokens = parser.parse();

            List<DrawEvent> pathEvents = new ArrayList<>();
            List<DrawEvent> imageEvents = new ArrayList<>();
            List<double[]> currentPathPoints = new ArrayList<>();
            List<Object> operands = new ArrayList<>();
            Deque<GraphicsState> stateStack = new ArrayDeque<>();

            boolean hasClip = false;
            boolean pendingClip = false;
            double[] ctm = identityMatrix();

            for (Object token : tokens) {
                if (!(token instanceof Operator)) {
                    operands.add(token);
                    continue;
                }

                Operator operator = (Operator) token;
                String op = operator.getName();

                switch (op) {
                    case "q":
                        stateStack.push(new GraphicsState(hasClip, pendingClip, ctm.clone()));
                        break;
                    case "Q":
                        if (!stateStack.isEmpty()) {
                            GraphicsState restored = stateStack.pop();
                            hasClip = restored.hasClip();
                            pendingClip = restored.pendingClip();
                            ctm = restored.ctm().clone();
                        }
                        currentPathPoints.clear();
                        break;
                    case "cm":
                        if (operands.size() >= 6) {
                            double[] transform = new double[]{
                                    operandAsDouble(operands, 0),
                                    operandAsDouble(operands, 1),
                                    operandAsDouble(operands, 2),
                                    operandAsDouble(operands, 3),
                                    operandAsDouble(operands, 4),
                                    operandAsDouble(operands, 5)
                            };
                            ctm = matrixMultiply(ctm, transform);
                        }
                        break;
                    case "W":
                    case "W*":
                        pendingClip = true;
                        break;
                    case "n":
                        if (pendingClip) {
                            hasClip = true;
                            pendingClip = false;
                        }
                        currentPathPoints.clear();
                        break;
                    case "m":
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 0), operandAsDouble(operands, 1));
                        break;
                    case "l":
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 0), operandAsDouble(operands, 1));
                        break;
                    case "c":
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 0), operandAsDouble(operands, 1));
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 2), operandAsDouble(operands, 3));
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 4), operandAsDouble(operands, 5));
                        break;
                    case "v":
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 0), operandAsDouble(operands, 1));
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 2), operandAsDouble(operands, 3));
                        break;
                    case "y":
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 0), operandAsDouble(operands, 1));
                        addPathPoint(currentPathPoints, ctm, operandAsDouble(operands, 2), operandAsDouble(operands, 3));
                        break;
                    case "re":
                        addRectanglePathPoints(
                                currentPathPoints,
                                ctm,
                                operandAsDouble(operands, 0),
                                operandAsDouble(operands, 1),
                                operandAsDouble(operands, 2),
                                operandAsDouble(operands, 3)
                        );
                        break;
                    case "S":
                    case "s":
                    case "f":
                    case "F":
                    case "f*":
                    case "B":
                    case "B*":
                    case "b":
                    case "b*":
                        if (!currentPathPoints.isEmpty()) {
                            pathEvents.add(new DrawEvent(pointsToBBox(currentPathPoints), hasClip || pendingClip));
                        }
                        if (pendingClip) {
                            hasClip = true;
                            pendingClip = false;
                        }
                        currentPathPoints.clear();
                        break;
                    case "Do":
                        double[] p0 = applyMatrix(ctm, 0, 0);
                        double[] p1 = applyMatrix(ctm, 1, 0);
                        double[] p2 = applyMatrix(ctm, 0, 1);
                        double[] p3 = applyMatrix(ctm, 1, 1);
                        imageEvents.add(new DrawEvent(pointsToBBox(List.of(p0, p1, p2, p3)), hasClip || pendingClip));
                        break;
                    default:
                        break;
                }

                operands.clear();
            }

            return new ParsedPageEvents(pathEvents, imageEvents);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse rendered PDF for clipping assertions", e);
        }
    }

    private static void addPathPoint(List<double[]> pathPoints, double[] ctm, double x, double y) {
        pathPoints.add(applyMatrix(ctm, x, y));
    }

    private static void addRectanglePathPoints(List<double[]> pathPoints, double[] ctm, double x, double y, double w, double h) {
        addPathPoint(pathPoints, ctm, x, y);
        addPathPoint(pathPoints, ctm, x + w, y);
        addPathPoint(pathPoints, ctm, x + w, y + h);
        addPathPoint(pathPoints, ctm, x, y + h);
    }

    private static double operandAsDouble(List<Object> operands, int index) {
        if (index >= operands.size()) {
            throw new IllegalStateException("Expected operand at index " + index + " but got " + operands.size());
        }
        Object operand = operands.get(index);
        if (operand instanceof COSNumber) {
            return ((COSNumber) operand).floatValue();
        }
        throw new IllegalStateException("Expected numeric operand but got: " + operand);
    }

    private static double[] identityMatrix() {
        return new double[]{1, 0, 0, 1, 0, 0};
    }

    private static double[] matrixMultiply(double[] left, double[] right) {
        double a1 = left[0];
        double b1 = left[1];
        double c1 = left[2];
        double d1 = left[3];
        double e1 = left[4];
        double f1 = left[5];

        double a2 = right[0];
        double b2 = right[1];
        double c2 = right[2];
        double d2 = right[3];
        double e2 = right[4];
        double f2 = right[5];

        return new double[]{
                a1 * a2 + c1 * b2,
                b1 * a2 + d1 * b2,
                a1 * c2 + c1 * d2,
                b1 * c2 + d1 * d2,
                a1 * e2 + c1 * f2 + e1,
                b1 * e2 + d1 * f2 + f1
        };
    }

    private static double[] applyMatrix(double[] matrix, double x, double y) {
        double a = matrix[0];
        double b = matrix[1];
        double c = matrix[2];
        double d = matrix[3];
        double e = matrix[4];
        double f = matrix[5];
        return new double[]{a * x + c * y + e, b * x + d * y + f};
    }

    private static double[] pointsToBBox(List<double[]> points) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (double[] point : points) {
            minX = Math.min(minX, point[0]);
            minY = Math.min(minY, point[1]);
            maxX = Math.max(maxX, point[0]);
            maxY = Math.max(maxY, point[1]);
        }
        return new double[]{minX, minY, maxX, maxY};
    }

    private static boolean bboxContainsPoint(double[] bbox, double x, double y, double tolerance) {
        return bbox[0] - tolerance <= x
                && x <= bbox[2] + tolerance
                && bbox[1] - tolerance <= y
                && y <= bbox[3] + tolerance;
    }

    private static double bboxArea(double[] bbox) {
        return Math.max(0, bbox[2] - bbox[0]) * Math.max(0, bbox[3] - bbox[1]);
    }

    private static double bboxIntersectionArea(double[] a, double[] b) {
        double left = Math.max(a[0], b[0]);
        double bottom = Math.max(a[1], b[1]);
        double right = Math.min(a[2], b[2]);
        double top = Math.min(a[3], b[3]);
        if (left >= right || bottom >= top) {
            return 0;
        }
        return (right - left) * (top - bottom);
    }

    private static String bboxToString(double[] bbox) {
        return String.format("[%.2f, %.2f, %.2f, %.2f]", bbox[0], bbox[1], bbox[2], bbox[3]);
    }

    // ===========================
    // Dump Helpers
    // ===========================

    private void dumpTextLines(int page) {
        List<TextLineReference> allLines = pdf.page(page).selectTextLines();
        System.err.printf("=== All TextLines on page %d (%d total) ===%n", page, allLines.size());
        for (int i = 0; i < allLines.size(); i++) {
            TextLineReference line = allLines.get(i);
            System.err.printf("[%d] text='%s' pos=(%f, %f) font='%s' size=%f color=%s%n",
                    i,
                    line.getText(),
                    line.getPosition().getX(),
                    line.getPosition().getY(),
                    line.getFontName(),
                    line.getFontSize(),
                    line.getColor());
        }
        System.err.println("=== End TextLines ===");
    }

    private void dumpParagraphs(int page) {
        List<TextParagraphReference> allParagraphs = pdf.page(page).selectParagraphs();
        System.err.printf("=== All Paragraphs on page %d (%d total) ===%n", page, allParagraphs.size());
        for (int i = 0; i < allParagraphs.size(); i++) {
            TextParagraphReference para = allParagraphs.get(i);
            String textPreview = para.getText();
            if (textPreview.length() > 50) {
                textPreview = textPreview.substring(0, 50) + "...";
            }
            textPreview = textPreview.replace("\n", "\\n");
            System.err.printf("[%d] text='%s' pos=(%f, %f) font='%s' size=%f color=%s%n",
                    i,
                    textPreview,
                    para.getPosition().getX(),
                    para.getPosition().getY(),
                    para.getFontName(),
                    para.getFontSize(),
                    para.getColor());
        }
        System.err.println("=== End Paragraphs ===");
    }

    private <T> T withTextLineDump(int page, java.util.function.Supplier<T> assertion) {
        try {
            return assertion.get();
        } catch (AssertionError e) {
            dumpTextLines(page);
            throw e;
        }
    }

    private <T> T withParagraphDump(int page, java.util.function.Supplier<T> assertion) {
        try {
            return assertion.get();
        } catch (AssertionError e) {
            dumpParagraphs(page);
            throw e;
        }
    }

    private static final class GraphicsState {
        private final boolean hasClip;
        private final boolean pendingClip;
        private final double[] ctm;

        private GraphicsState(boolean hasClip, boolean pendingClip, double[] ctm) {
            this.hasClip = hasClip;
            this.pendingClip = pendingClip;
            this.ctm = ctm;
        }

        private boolean hasClip() {
            return hasClip;
        }

        private boolean pendingClip() {
            return pendingClip;
        }

        private double[] ctm() {
            return ctm;
        }
    }

    private static final class DrawEvent {
        private final double[] bbox;
        private final boolean clipped;

        private DrawEvent(double[] bbox, boolean clipped) {
            this.bbox = bbox;
            this.clipped = clipped;
        }

        private double[] bbox() {
            return bbox;
        }

        private boolean clipped() {
            return clipped;
        }
    }

    private static final class ParsedPageEvents {
        private final List<DrawEvent> pathEvents;
        private final List<DrawEvent> imageEvents;

        private ParsedPageEvents(List<DrawEvent> pathEvents, List<DrawEvent> imageEvents) {
            this.pathEvents = pathEvents;
            this.imageEvents = imageEvents;
        }

        private List<DrawEvent> pathEvents() {
            return pathEvents;
        }

        private List<DrawEvent> imageEvents() {
            return imageEvents;
        }
    }
}
