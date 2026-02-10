package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("NewClassNamingConvention")
public class FontEncoding_50e14fce_5c630864_Test extends BaseTest {

    @Test
    public void modifyLineWithCustomFontEncoding() {
        PDFDancer pdf = createClient();
        File ttfFile = new File("src/test/resources/fixtures/SourceSans3-Regular.ttf");
        pdf.registerFont(ttfFile);

        TextLineReference line = pdf.page(1).selectTextLines().get(0);

        PdfDancerClientException ex = assertThrows(PdfDancerClientException.class, () ->
                line.edit()
                        .replace("\uF020")
                        .font("SourceSans3-Regular", 12.0)
                        .apply());

        assertTrue(ex.getMessage().contains("No glyph for U+F020"));
    }
}
