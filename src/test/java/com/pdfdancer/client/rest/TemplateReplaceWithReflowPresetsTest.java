package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ReflowPreset;
import com.pdfdancer.common.request.TemplateReplaceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TemplateReplaceWithReflowPresetsTest extends BaseTest {

    private PDFDancer loadFixture() throws IOException {
        byte[] pdfBytes = Files.readAllBytes(new File("src/test/examples/text/three-column-paragraphs.pdf").toPath());
        return PDFDancer.createSession(getValidToken(), pdfBytes, httpClient);
    }

    @Test
    public void testNoReflowKeepsLine() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.NONE)
                        .replace("Left aligned text starts each measure with a", "YABBA!")
                        .build()
        );
        assertTrue(success);
        PDFAssertions pdfAssertions = new PDFAssertions(client);
        pdfAssertions.assertTextlineExists("YABBA", 1);
        TextLineReference textLine = pdfAssertions.findTextLine("YABBA!", 1);
        assertNotNull(textLine);
        assertEquals("YABBA!", textLine.getText());
    }

    @Test
    public void testReflowReflows() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.BEST_EFFORT)
                        .replace("Left aligned text starts each measure with a", "YABBA!")
                        .build()
        );
        assertTrue(success);
        PDFAssertions pdfAssertions = new PDFAssertions(client);
        pdfAssertions.assertTextlineExists("YABBA", 1);
        TextLineReference textLine = pdfAssertions.findTextLineStartingWith("YABBA!", 1);
        assertNotNull(textLine);
        assertEquals("YABBA! stable edge, making the rhythm of", textLine.getText());
    }


    @Test
    public void testReflowFailOrFitFails() throws IOException {
        PDFDancer client = loadFixture();
        assertThrows(PdfDancerClientException.class, () -> {
            client.applyReplacements(
                    TemplateReplaceRequest.builder()
                            .reflowPreset(ReflowPreset.FIT_OR_FAIL)
                            .replace("Left aligned text starts each measure with a", "YABBAYABBAYABBAYABBAYABBAYABBAYABBAYABBAYABBAYABBA!")
                            .build()
            );
        });
    }

    @Test
    public void testReflowFailOrFitFits() throws IOException {
        PDFDancer client = loadFixture();

        boolean success = client.applyReplacements(
                TemplateReplaceRequest.builder()
                        .reflowPreset(ReflowPreset.FIT_OR_FAIL)
                        .replace("Left aligned text starts each measure with a", "YABBA!")
                        .build()
        );
        assertTrue(success);
        PDFAssertions pdfAssertions = new PDFAssertions(client);
        pdfAssertions.assertTextlineExists("YABBA", 1);
        TextLineReference textLine = pdfAssertions.findTextLineStartingWith("YABBA!", 1);
        assertNotNull(textLine);
        assertEquals("YABBA! stable edge, making the rhythm of", textLine.getText());
    }
}
