package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.*;
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
        assertFalse(objectRefs.isEmpty());
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

}
