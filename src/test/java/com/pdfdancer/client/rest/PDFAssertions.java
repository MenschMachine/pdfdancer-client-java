package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Color;
import com.pdfdancer.common.model.Orientation;
import com.pdfdancer.common.model.PageRef;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    public PDFAssertions(PDFDancer pdfDancer) {
        this(pdfDancer, "42", pdfDancer.getHttpClient());
    }

    public PDFAssertions(PDFDancer pdfDancer, String token, PdfDancerHttpClient httpClient) {
        this.token = token;
        this.httpClient = httpClient;

        // Save and reload to get fresh state (matches Python behavior)
        try {
            File tempFile = File.createTempFile("client-assertions-", ".client");
            pdfDancer.save(tempFile.getAbsolutePath());
            System.out.println("Saved client file to " + tempFile.getAbsolutePath());

            // Reload from the saved file
            byte[] pdfBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
            this.pdf = PDFDancer.createSession(token, pdfBytes, httpClient);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PDFAssertions", e);
        }
    }

    // ===========================
    // Text Assertions
    // ===========================

    public PDFAssertions assertTextHasColor(String text, Color color, int page) {
        assertTextlineHasColor(text, color, page);

        List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(text);
        assertEquals(1, paragraphs.size(),
                String.format("Expected 1 paragraph but got %d", paragraphs.size()));

        TextParagraphReference ref = paragraphs.get(0);
        assertTrue(ref.getText().contains(text));
        assertEquals(color, ref.getColor(),
                String.format("%s != %s", color, ref.getColor()));

        return this;
    }

    public PDFAssertions assertTextHasFont(String text, String fontName, double fontSize, int page) {
        assertTextlineHasFont(text, fontName, fontSize, page);

        List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
        assertEquals(1, paragraphs.size(),
                String.format("Expected 1 paragraph but got %d", paragraphs.size()));

        TextParagraphReference ref = paragraphs.get(0);
        assertEquals(fontName, ref.getFontName(),
                String.format("Expected %s to match %s", ref.getFontName(), fontName));
        assertEquals(fontSize, ref.getFontSize(), 0.001);

        return this;
    }

    public PDFAssertions assertParagraphIsAt(String text, double x, double y, int page) {
        return assertParagraphIsAt(text, x, y, page, 0.01);
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
        List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
        assertEquals(1, lines.size(),
                String.format("Expected 1 line but got %d", lines.size()));

        TextLineReference ref = lines.get(0);
        assertEquals(color, ref.getColor(),
                String.format("%s != %s", color, ref.getColor()));
        assertTrue(ref.getText().contains(text));

        return this;
    }

    public PDFAssertions assertTextlineHasFont(String text, String fontName, double fontSize, int page) {
        List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
        assertEquals(1, lines.size(),
                String.format("Expected 1 line but got %d", lines.size()));

        TextLineReference ref = lines.get(0);
        assertEquals(fontName, ref.getFontName(),
                String.format("Expected %s but got %s", fontName, ref.getFontName()));
        assertEquals(fontSize, ref.getFontSize(), 0.001,
                String.format("%f != %f", fontSize, ref.getFontSize()));

        return this;
    }

    public PDFAssertions assertTextlineHasFontMatching(String text, String fontName, double fontSize, int page) {
        List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
        assertEquals(1, lines.size(),
                String.format("Expected 1 line but got %d", lines.size()));

        TextLineReference ref = lines.get(0);
        assertTrue(ref.getFontName().contains(fontName),
                String.format("Expected %s to match %s", ref.getFontName(), fontName));
        assertEquals(fontSize, ref.getFontSize(), 0.001);

        return this;
    }

    public PDFAssertions assertTextlineIsAt(String text, double x, double y, int page) {
        return assertTextlineIsAt(text, x, y, page, 1e-6);
    }

    public PDFAssertions assertTextlineIsAt(String text, double x, double y, int page, double epsilon) {
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
    }

    public PDFAssertions assertTextlineDoesNotExist(String text, int page) {
        List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
        assertEquals(0, lines.size());
        return this;
    }

    public PDFAssertions assertTextlineExists(String text, int page) {
        List<TextLineReference> lines = pdf.page(page).selectTextLinesStartingWith(text);
        assertEquals(1, lines.size());
        return this;
    }

    public PDFAssertions assertParagraphExists(String text, int page) {
        List<TextParagraphReference> paragraphs = pdf.page(page).selectParagraphsMatching(".*" + text + ".*");
        assertEquals(1, paragraphs.size(),
                String.format("No paragraphs starting with %s found on page %d", text, page));
        return this;
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

    public PDFAssertions assertPageDimension(double width, double height, int pageIndex) {
        return assertPageDimension(width, height, null, pageIndex);
    }

    public PDFAssertions assertPageDimension(double width, double height, Orientation orientation, int pageIndex) {
        PageRef page = pdf.getPages().get(pageIndex);
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
                .filter(p -> p.getPosition().getPageIndex() == page)
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
            int pageIndex = pageRef.getPosition().getPageIndex();
            total += getNumberOfElements(pageIndex);
        }
        assertEquals(nrOfElements, total,
                String.format("Total number of elements differ, actual %d != expected %d", total, nrOfElements));
        return this;
    }

    public PDFAssertions assertTotalNumberOfElements(int nrOfElements, int pageIndex) {
        int total = getNumberOfElements(pageIndex);
        assertEquals(nrOfElements, total,
                String.format("Total number of elements differ, actual %d != expected %d", total, nrOfElements));
        return this;
    }

    private int getNumberOfElements(int pageIndex) {
        int total = 0;
        total += pdf.page(pageIndex).selectParagraphs().size();
        total += pdf.page(pageIndex).selectFormFields().size();
        total += pdf.page(pageIndex).selectForms().size();
        total += pdf.page(pageIndex).selectImages().size();
        total += pdf.page(pageIndex).selectPaths().size();
        total += pdf.page(pageIndex).selectTextLines().size();
        return total;
    }

    // ===========================
    // Form Assertions
    // ===========================

    public PDFAssertions assertNumberOfFormxobjects(int nrOfFormxobjects, int pageIndex) {
        assertEquals(nrOfFormxobjects, pdf.page(pageIndex).selectForms().size(),
                String.format("Expected nr of formxobjects %d but got %d",
                        nrOfFormxobjects, pdf.page(pageIndex).selectForms().size()));
        return this;
    }

    public PDFAssertions assertNumberOfFormFields(int nrOfFormFields, int pageIndex) {
        assertEquals(nrOfFormFields, pdf.page(pageIndex).selectFormFields().size(),
                String.format("Expected nr of form fields %d but got %d",
                        nrOfFormFields, pdf.page(pageIndex).selectFormFields().size()));
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

    public PDFAssertions assertFormFieldExists(String fieldName, int pageIndex) {
        List<FormFieldReference> formFields = pdf.selectFormFieldsByName(fieldName);
        // Filter to page
        long pageFormFields = formFields.stream()
                .filter(f -> f.getPosition().getPageIndex() == pageIndex)
                .count();
        assertEquals(1, pageFormFields,
                String.format("Expected 1 form field but got %d", pageFormFields));
        return this;
    }

    public PDFAssertions assertFormFieldHasValue(String fieldName, String fieldValue, int pageIndex) {
        List<FormFieldReference> formFields = pdf.selectFormFieldsByName(fieldName);
        // Filter to page
        List<FormFieldReference> pageFormFields = formFields.stream()
                .filter(f -> f.getPosition().getPageIndex() == pageIndex)
                .collect(Collectors.toUnmodifiableList());
        assertEquals(1, pageFormFields.size(),
                String.format("Expected 1 form field but got %d", pageFormFields.size()));
        assertEquals(fieldValue, pageFormFields.get(0).getValue(),
                String.format("%s != %s", pageFormFields.get(0).getValue(), fieldValue));
        return this;
    }

    public void assertParagraphHasColor(String text, Color color, int pageIndex) {
        TextParagraphReference paragraph = pdf.page(pageIndex).selectParagraphsStartingWith(text).get(0);
        assertEquals(color, paragraph.getColor(),
                String.format("%s != %s", color, paragraph.getColor()));
    }
}
