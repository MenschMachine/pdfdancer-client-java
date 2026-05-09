package com.pdfdancer.client.rest;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdamsKnightTest extends BaseTest {

    @Test
    public void replaceMemberWithoutFont() {

        Map<String, String> replacements = new HashMap<>();
        replacements.put("XYZmember", "National Treasury Employees employee");

        PDFDancer pdf = createClient("PrintCollateral_01.pdf");
        assertNotNull(pdf);
        for (String s : replacements.keySet()) {
            String newText = replacements.get(s);
            boolean apply = pdf.replace("{" + s + "}", newText).apply();
            assertTrue(apply, "Could not replace '" + s + "' with " + newText);
        }
        PDFAssertions pdfAssertions = new PDFAssertions(pdf);
        for (String s : replacements.keySet()) {
            pdfAssertions.assertTextlineExists(".*" + replacements.get(s) + ".*", 1);
        }
    }

    @Test
    public void replaceMemberWithFont() {

        Map<String, String> replacements = getReplacements();

        PDFDancer pdf = createClient("PrintCollateral_01.pdf");
        assertNotNull(pdf);

        for (String fontName : List.of("BattersonSans-Black.ttf", "BattersonSans-Regular.ttf", "BattersonSlab-Light.ttf", "BattersonSlab-Black.ttf")) {
            pdf.registerFont(new File("src/test/resources/fixtures/fonts/" + fontName));
        }

        for (TextLineReference line : pdf.selectTextLines()) {
            for (String s : replacements.keySet()) {
                if (line.getText().contains(s)) {
                    System.out.println(line.getFontName() + ": " + line.getText());
                }
            }
        }

        for (String s : replacements.keySet()) {
            String newText = replacements.get(s);
            boolean apply = pdf.replace(s, newText)
                    .apply();
            assertTrue(apply, "Could not replace '" + s + "' with " + newText);
        }
        PDFAssertions pdfAssertions = new PDFAssertions(pdf);
        for (String s : replacements.keySet()) {
            for (String line : replacements.get(s).split("\\R", -1)) {
                if (!line.isEmpty()) {
                    pdfAssertions.assertTextlineExists(".*" + line + ".*", 1);
                }
            }
        }
    }

    private static Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{XYZmember}", "National Treasury Employees employee");
        replacements.put("{XYZemployees} switch to the Travelers Auto Insurance Program", "National Treasury Employees members switch to the Travelers Auto\nInsurance Program");
        replacements.put("{XYZphone}", "888.666.6062");
        replacements.put("{XYZurlpath}", "NTEU");
        replacements.put("{XYZConvenientCustomized}", "Convenient Payroll Deduction / Customized Coverage Options");
        return replacements;
    }
}
