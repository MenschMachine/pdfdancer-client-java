package com.tfc.pdf.pdfdancer.api.client.rest;

import com.tfc.pdf.pdfdancer.api.common.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PDFTest extends BaseTest {

    @Test
    public void createBlankPdf() {
        PDFDancer pdf = newPdf();
        List<ObjectRef> objectRefs = pdf.selectElements();
        assertEquals(0, objectRefs.size());
    }

    @Test
    public void selectElements() {
        PDFDancer pdf = createClient();
        List<ObjectRef> objectRefs = pdf.selectElements();
        assertEquals(638, objectRefs.size());
    }

    @Test
    public void testCreateBlankPdfDefaults() {
        PDFDancer pdf = TestPDFDancer.newPdf(getValidToken(), httpClient);
        List<PageRef> pages = pdf.getPages();
        assertEquals(1, pages.size(), "Default blank PDF should have 1 page");

        byte[] pdfBytes = pdf.getFileBytes();
        assertTrue(pdfBytes.length > 0, "PDF bytes should not be empty");
        assertEquals('%', pdfBytes[0], "PDF should start with PDF signature");
        assertEquals('P', pdfBytes[1]);
        assertEquals('D', pdfBytes[2]);
        assertEquals('F', pdfBytes[3]);
    }

    @Test
    public void testCreateBlankPdfWithCustomParams() {
        PDFDancer pdf = PDFDancer.createNew(
                getValidToken(),
                PageSize.A4,
                Orientation.LANDSCAPE,
                3,
                httpClient
        );
        List<PageRef> pages = pdf.getPages();
        assertEquals(3, pages.size(), "PDF should have 3 pages");

        new PDFAssertions(pdf).assertPageCount(3);
    }

    @Test
    public void testCreateBlankPdfWithLetterSize() {
        PDFDancer pdf = PDFDancer.createNew(
                getValidToken(),
                PageSize.LETTER,
                Orientation.PORTRAIT,
                2,
                httpClient
        );
        List<PageRef> pages = pdf.getPages();
        assertEquals(2, pages.size(), "PDF should have 2 pages");

        new PDFAssertions(pdf).assertPageCount(2);
    }

    @Test
    public void testCreateBlankPdfAddContent() {
        PDFDancer pdf = TestPDFDancer.newPdf(getValidToken(), httpClient);
        pdf.newParagraph()
                .text("Hello from blank PDF")
                .font("Courier-BoldOblique", 9)
                .color(new Color(0, 255, 0))
                .at(0, 100, 201.5)
                .add();

        List<TextParagraphReference> paragraphs = pdf.selectParagraphs();
        assertEquals(1, paragraphs.size(), "Should have one paragraph");

        new PDFAssertions(pdf).assertParagraphIsAt("Hello from blank PDF", 100, 201.5, 0);
    }

    @Test
    public void testCreateBlankPdfAddAndModifyContent() {
        PDFDancer pdf = TestPDFDancer.newPdf(getValidToken(), httpClient);
        pdf.newParagraph()
                .text("Hello from blank PDF")
                .font("Courier-BoldOblique", 9)
                .color(new Color(128, 56, 127))
                .at(0, 100, 201.5)
                .add();

        List<TextLineReference> selectedLines = pdf.page(0).selectTextLineAt(100, 201.5, 3d); // needs high tolerance, because y of line != y of paragraph
        assertEquals(1, selectedLines.size());
        assertNotNull(selectedLines.getFirst().getInternalId());
        pdf.save("/tmp/test_create_blank_pdf_add_and_modify_content.pdf");

        try {
            PDFDancer pdf2 = PDFDancer.createSession(
                    getValidToken(),
                    java.nio.file.Files.readAllBytes(
                            java.nio.file.Paths.get("/tmp/test_create_blank_pdf_add_and_modify_content.pdf")
                    ),
                    httpClient
            );
            for (int i = 0; i < 10; i++) {
                List<TextLineReference> selectedLines2 = pdf2.page(0).selectTextLineAt(100, 201.5, 3);
                assertEquals(1, selectedLines2.size());
                TextLineReference line = selectedLines2.getFirst();
                boolean success = line.edit().replace("hello " + i);
                assertTrue(success);
            }
            pdf2.save("/tmp/test_create_blank_pdf_add_and_modify_content2.pdf");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
}
