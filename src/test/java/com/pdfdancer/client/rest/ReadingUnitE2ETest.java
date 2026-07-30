package com.pdfdancer.client.rest;

import com.pdfdancer.common.response.ReadingUnit;
import com.pdfdancer.common.response.ReadingUnitDocumentAnalysis;
import com.pdfdancer.common.response.ReadingUnitMode;
import com.pdfdancer.common.response.ReadingUnitPageAnalysis;
import com.pdfdancer.common.response.ReadingUnitRole;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ReadingUnitE2ETest extends BaseTest {
    @Test
    void documentAndPageAnalysesExposeCompletePageData() {
        PDFDancer pdf = createClient("Showcase.pdf");
        ReadingUnitDocumentAnalysis document = pdf.analyzeReadingUnits();

        assertEquals(ReadingUnitMode.PRIMARY, document.mode());
        assertTrue(document.pageCount() > 0);
        assertEquals(document.pageCount(), document.pages().size());
        assertEquals(IntStream.rangeClosed(1, document.pageCount()).boxed().toList(),
                document.pages().stream().map(ReadingUnitPageAnalysis::pageNumber).toList());

        ReadingUnitPageAnalysis page = pdf.page(1).analyzeReadingUnits();
        assertEquals(document.pages().get(0), page);
        assertFalse(page.units().isEmpty());

        ReadingUnit unit = page.units().get(0);
        assertNotEquals(ReadingUnitRole.UNKNOWN, unit.role());
        assertNotNull(unit.rawRole());
        assertFalse(unit.id().isBlank());
        assertNotNull(unit.text());
        assertEquals(1, unit.provenance().pageNumber());
        assertFalse(unit.provenance().sourceElementIds().isEmpty());
        assertTrue(unit.provenance().bounds().width() >= 0);
        assertTrue(unit.provenance().bounds().height() >= 0);
        assertTrue(unit.stream().get(ReadingUnitMode.PRIMARY).included());
        assertTrue(unit.stream().get(ReadingUnitMode.PRIMARY).order() > 0);
    }

    @Test
    void eachReadingUnitAnalysisCallIsFresh() {
        PDFDancer pdf = createClient("Showcase.pdf");
        assertEquals(pdf.page(1).analyzeReadingUnits(), pdf.page(1).analyzeReadingUnits());
    }
}
