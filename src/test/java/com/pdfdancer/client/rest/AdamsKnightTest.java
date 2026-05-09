package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PositionBuilder;
import com.pdfdancer.common.model.Size;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdamsKnightTest extends BaseTest {

    private static final String ADAMS_KNIGHT_FIXTURE_DIR = "src/test/resources/fixtures/adamsknight/";
    private static final int PAGE_NUMBER = 1;

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
    public void replaceMemberWithFont() throws IOException {

        Map<String, String> replacements = getReplacements();

        PDFDancer pdf = createClient("PrintCollateral_01.pdf");
        assertNotNull(pdf);

        for (String fontName : List.of("BattersonSans-Black.ttf", "BattersonSans-Regular.ttf", "BattersonSlab-Light.ttf", "BattersonSlab-Black.ttf")) {
            pdf.registerFont(new File(ADAMS_KNIGHT_FIXTURE_DIR + fontName));
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
        replaceLogo(pdf);
        replaceQrCode(pdf);

        PDFAssertions pdfAssertions = new PDFAssertions(pdf);
        pdfAssertions.assertTextlineDoesNotExist("{XYZlogo}", PAGE_NUMBER);
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

    private void replaceLogo(PDFDancer pdf) throws IOException {
        int imageCountBefore = pdf.page(PAGE_NUMBER).selectImages().size();
        File logo = new File(ADAMS_KNIGHT_FIXTURE_DIR + "x1NTEUlogoweb.jpg");

        boolean replaced = pdf.replaceWithImage("{XYZlogo}", logo, 77.5, 30).apply();
        assertTrue(replaced, "Could not replace '{XYZlogo}' with logo image");
        assertEquals(imageCountBefore + 1, pdf.page(PAGE_NUMBER).selectImages().size());
    }

    private void replaceQrCode(PDFDancer pdf) throws IOException {
        BoundingRect qrBounds = findExistingQrCodeBounds(pdf);
        int imageCountBefore = pdf.page(PAGE_NUMBER).selectImages().size();

        PathGroupReference qrCode = pdf.page(PAGE_NUMBER).groupPathsInRegion(qrBounds);
        assertNotNull(qrCode, "Could not find the existing QR code paths");
        assertTrue(qrCode.getPathCount() > 100, "QR code path group should include the QR path cluster");
        assertTrue(qrCode.remove(), "Could not remove the existing QR code");

        Image replacementQrCode = Image.fromFile(new File(ADAMS_KNIGHT_FIXTURE_DIR + "new_qr.png"));
        replacementQrCode.setSize(new Size(qrBounds.getWidth(), qrBounds.getHeight()));

        boolean added = pdf.addImage(replacementQrCode,
                new PositionBuilder()
                        .onPage(PAGE_NUMBER)
                        .atCoordinates(qrBounds.getX(), qrBounds.getY())
                        .build());
        assertTrue(added, "Could not add replacement QR code");
        assertEquals(imageCountBefore + 1, pdf.page(PAGE_NUMBER).selectImages().size());
    }

    private BoundingRect findExistingQrCodeBounds(PDFDancer pdf) {
        return pdf.page(PAGE_NUMBER).selectPaths().stream()
                .map(path -> path.getPosition().getBoundingRect())
                .filter(this::isQrCodeBoundary)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not find existing QR code bounds"));
    }

    private boolean isQrCodeBoundary(BoundingRect rect) {
        return rect != null
                && rect.getX() > 280 && rect.getX() < 290
                && rect.getY() > 125 && rect.getY() < 135
                && rect.getWidth() > 40 && rect.getWidth() < 50
                && rect.getHeight() > 40 && rect.getHeight() < 50;
    }
}
