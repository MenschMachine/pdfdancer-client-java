package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PositionBuilder;
import com.pdfdancer.common.model.Size;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdamsKnightTest extends BaseTest {

    private static final String ADAMS_KNIGHT_FIXTURE_DIR = "src/test/resources/fixtures/adamsknight/";
    private static final int PAGE_NUMBER = 1;

    @Test
    public void replaceMemberWithFont() throws IOException {

        Map<String, String> replacements = getReplacements();

        PDFDancer pdf = createClient("adamsknight/PrintCollateral_01.pdf");
        assertNotNull(pdf);

        for (String fontName : List.of("BattersonSans-Black.ttf", "BattersonSans-Regular.ttf", "BattersonSlab-Light.ttf", "BattersonSlab-Black.ttf")) {
            pdf.registerFont(new File(ADAMS_KNIGHT_FIXTURE_DIR + fontName));
        }

        for (TextLineReference line : pdf.selectTextLines()) {
            String text = line.getText();
            if (text == null) {
                continue;
            }
            for (String s : replacements.keySet()) {
                if (text.contains(s)) {
                    System.out.println(line.getFontName() + ": " + text);
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
        pdfAssertions.assertTextlineDoesNotExist("{{logo_url}}", PAGE_NUMBER);
        pdfAssertions.assertTextlineDoesNotExist("{{sponsorqrcode}}", PAGE_NUMBER);
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
        replacements.put("{{partner_name}} employee", "National Treasury Employees employee");
        replacements.put("{{partner_name}} members switch to the Travelers Auto Insurance Program", "National Treasury Employees members switch to the Travelers Auto\nInsurance Program");
        replacements.put("{{tfn}}", "888.666.6062");
        replacements.put("{{sponsorid}}", "NTEU");
        return replacements;
    }

    private void replaceLogo(PDFDancer pdf) throws IOException {
        List<ImageReference> imagesBefore = pdf.page(PAGE_NUMBER).selectImages();
        File logo = new File(ADAMS_KNIGHT_FIXTURE_DIR + "x1NTEUlogoweb.jpg");
        File pipeLogo = new File(ADAMS_KNIGHT_FIXTURE_DIR + "Travelers_PipeLogo.png");

        boolean replaced = pdf.replaceWithImage("{{logo_url}}", logo, 77.5, 30).apply();
        assertTrue(replaced, "Could not replace '{{logo_url}}' with logo image");

        List<ImageReference> imagesAfter = pdf.page(PAGE_NUMBER).selectImages();
        List<String> imageIdsBefore = imagesBefore.stream().map(ImageReference::getInternalId).collect(Collectors.toList());
        ImageReference logoReplacement = imagesAfter.stream()
                .filter(image -> !imageIdsBefore.contains(image.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not locate replaced logo image"));

        assertEquals(imagesBefore.size() + 1, imagesAfter.size());

        if (pipeLogo.exists()) {
            Image overlay = Image.fromFile(pipeLogo);
            Size originalSize = overlay.getSize();
            double width = originalSize != null && originalSize.getHeight() > 0
                    ? originalSize.getWidth() * 30 / originalSize.getHeight()
                    : 77.5;
            overlay.setSize(new Size(width, 30));

            Double logoX = logoReplacement.getPosition().getX();
            Double logoY = logoReplacement.getPosition().getY();
            Double logoWidth = logoReplacement.getWidth();
            assertNotNull(logoX, "Logo replacement has no x coordinate");
            assertNotNull(logoY, "Logo replacement has no y coordinate");
            assertNotNull(logoWidth, "Logo replacement has no width");

            boolean pipeLogoAdded = pdf.addImage(
                    overlay,
                    new PositionBuilder()
                            .onPage(PAGE_NUMBER)
                            .atCoordinates(logoX + logoWidth + 15.0, logoY)
                            .build()
            );
            assertTrue(pipeLogoAdded, "Could not add Travelers_PipeLogo.png");
        }

        if (pipeLogo.exists()) {
            assertEquals(imagesBefore.size() + 2, pdf.page(PAGE_NUMBER).selectImages().size());
        }
    }

    private void replaceQrCode(PDFDancer pdf) throws IOException {
        List<ImageReference> imagesBefore = pdf.page(PAGE_NUMBER).selectImages();
        File qrCode = new File(ADAMS_KNIGHT_FIXTURE_DIR + "new_qr.png");

        boolean replaced = pdf.replaceWithImage("{{sponsorqrcode}}", qrCode, 50, 50).apply();
        assertTrue(replaced, "Could not replace '{{sponsorqrcode}}' with QR code image");
        List<ImageReference> imagesAfter = pdf.page(PAGE_NUMBER).selectImages();
        List<String> imageIdsBefore = imagesBefore.stream().map(ImageReference::getInternalId).collect(Collectors.toList());
        ImageReference qrImage = imagesAfter.stream()
                .filter(image -> !imageIdsBefore.contains(image.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not locate inserted QR code image"));

        moveInsertedQrCode(qrImage);
        assertEquals(imagesBefore.size() + 1, imagesAfter.size());
    }

    private void moveInsertedQrCode(ImageReference qrImage) {
        Double qrImageX = qrImage.getPosition().getX();
        Double qrImageY = qrImage.getPosition().getY();
        assertNotNull(qrImageX, "QR code image has no x coordinate");
        assertNotNull(qrImageY, "QR code image has no y coordinate");

        assertTrue(qrImage.moveTo(qrImageX + 30, qrImageY - 50), "Could not move QR code image");
    }
}
