package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.ObjectRef;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageRef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fluent assertion helper for PDF validation.
 * Matches the Python PDFAssertions API for consistent testing across clients.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class PDFAssertions {

    private static final Logger log = LoggerFactory.getLogger(PDFAssertions.class);

    private final PDFDancer pdf;
    private final String token;
    private final PdfDancerHttpClient httpClient;

    public PDFAssertions(PDFDancer pdfDancer) {
        this(pdfDancer, "42", pdfDancer.getHttpClient());
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

    public PDFAssertions assertTextlineExists(String text, int page) {
        return withTextLineDump(page, () -> {
            List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
            assertEquals(1, lines.size());
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

    // ===========================
    // Dump Helpers
    // ===========================

    private void dumpTextLines(int page) {
        List<TextLineReference> allLines = pdf.page(page).selectTextLines();
        log.debug("=== All TextLines on page {} ({} total) ===", page, allLines.size());
        for (int i = 0; i < allLines.size(); i++) {
            TextLineReference line = allLines.get(i);
            log.debug("[{}] text='{}' pos=({}, {}) font='{}' size={} color={}",
                    i,
                    line.getText(),
                    line.getPosition().getX(),
                    line.getPosition().getY(),
                    line.getFontName(),
                    line.getFontSize(),
                    line.getColor());
        }
        log.debug("=== End TextLines ===");
    }

    private void dumpParagraphs(int page) {
        List<TextParagraphReference> allParagraphs = pdf.page(page).selectParagraphs();
        log.debug("=== All Paragraphs on page {} ({} total) ===", page, allParagraphs.size());
        for (int i = 0; i < allParagraphs.size(); i++) {
            TextParagraphReference para = allParagraphs.get(i);
            String textPreview = para.getText();
            if (textPreview.length() > 50) {
                textPreview = textPreview.substring(0, 50) + "...";
            }
            textPreview = textPreview.replace("\n", "\\n");
            log.debug("[{}] text='{}' pos=({}, {}) font='{}' size={} color={}",
                    i,
                    textPreview,
                    para.getPosition().getX(),
                    para.getPosition().getY(),
                    para.getFontName(),
                    para.getFontSize(),
                    para.getColor());
        }
        log.debug("=== End Paragraphs ===");
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
}
