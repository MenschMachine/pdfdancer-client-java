package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.PositionBuilder;
import com.pdfdancer.common.model.Size;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final double PAGE_WIDTH = 612.0;

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
            handleOverflowingLines(pdf, newText);
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
        replacements.put("{{partner_name}} members switch to the Travelers Auto Insurance Program", "National Treasury Employees members switch to the Travelers Auto Insurance Program");
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
        ImageReference qrImage = locateInsertedQrCodeImage(imagesBefore, imagesAfter);

        moveInsertedQrCode(qrImage);
        assertEquals(imagesBefore.size() + 1, imagesAfter.size());
    }

    private ImageReference locateInsertedQrCodeImage(List<ImageReference> imagesBefore, List<ImageReference> imagesAfter) {
        List<String> imageIdsBefore = imagesBefore.stream().map(ImageReference::getInternalId).collect(Collectors.toList());
        return imagesAfter.stream()
                .filter(image -> !imageIdsBefore.contains(image.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Could not locate inserted QR code image"));
    }

    private void moveInsertedQrCode(ImageReference qrImage) {
        Double qrImageX = qrImage.getPosition().getX();
        Double qrImageY = qrImage.getPosition().getY();
        assertNotNull(qrImageX, "QR code image has no x coordinate");
        assertNotNull(qrImageY, "QR code image has no y coordinate");

        assertTrue(qrImage.moveTo(qrImageX + 12, qrImageY - 31), "Could not move QR code image");
    }

    private void handleOverflowingLines(PDFDancer pdf, String replacementText) {
        List<TextLineReference> overflowingLines = pdf.page(PAGE_NUMBER).selectTextLines().stream()
                .filter(line -> line.getText() != null && line.getText().contains(replacementText))
                .filter(this::isOverflowing)
                .collect(Collectors.toList());
        for (TextLineReference line : overflowingLines) {
            splitOverflowingLine(pdf, line);
        }
    }

    private boolean isOverflowing(TextLineReference line) {
        if (line.getPosition() == null || line.getPosition().getBoundingRect() == null) {
            return false;
        }
        return line.getPosition().getBoundingRect().getX() + line.getPosition().getBoundingRect().getWidth() > PAGE_WIDTH;
    }

    private void splitOverflowingLine(PDFDancer pdf, TextLineReference line) {
        String text = line.getText();
        if (text == null || text.contains("\n")) {
            return;
        }
        List<String> splitLines = splitOverflowingText(line, text);
        if (splitLines.size() <= 1) {
            return;
        }

        assertTrue(line.edit().replace(String.join("\n", splitLines)).apply(), "Could not split overflowed line");
        moveOverflowContinuationLine(pdf, line, splitLines.get(1));
    }

    private List<String> splitOverflowingText(TextLineReference line, String text) {
        Double fontSizeObj = line.getFontSize();
        double fontSize = fontSizeObj == null ? 12.0 : Math.abs(fontSizeObj);

        if (line.getPosition() == null || line.getPosition().getBoundingRect() == null || text == null) {
            return List.of(text == null ? "" : text);
        }
        if (line.getPosition().getBoundingRect().getX() >= PAGE_WIDTH) {
            return List.of(text);
        }

        double availableWidth = PAGE_WIDTH - line.getPosition().getBoundingRect().getX();
        double avgCharWidth = Math.max(fontSize * 0.52, 1.0);
        int maxChars = Math.max(10, (int) Math.floor(availableWidth / avgCharWidth));
        if (text.length() <= maxChars) {
            return List.of(text);
        }

        String[] words = text.split(" ");
        if (words.length == 0 || words.length == 1) {
            return fallbackSplit(text);
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.length() == 0) {
                currentLine.append(word);
                continue;
            }
            if (currentLine.length() + 1 + word.length() > maxChars) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(" ").append(word);
            }
        }
        if (currentLine.length() != 0) {
            lines.add(currentLine.toString());
        }
        if (lines.size() <= 1) {
            return fallbackSplit(text);
        }
        return lines;
    }

    private List<String> fallbackSplit(String text) {
        if (text == null || text.isBlank()) {
            return List.of(text);
        }
        int mid = text.length() / 2;
        int left = text.lastIndexOf(" ", mid);
        int right = text.indexOf(" ", mid);
        int splitAt = right != -1 && left != -1 ? (right - mid) < (mid - left) ? right : left : right != -1 ? right : left;
        if (splitAt == -1 || splitAt <= 0 || splitAt >= text.length() - 1) {
            return List.of(text);
        }
        String first = text.substring(0, splitAt).trim();
        String second = text.substring(splitAt + 1).trim();
        if (first.isBlank() || second.isBlank()) {
            return List.of(text);
        }
        return List.of(first, second);
    }

    private void moveOverflowContinuationLine(PDFDancer pdf, TextLineReference originalLine, String continuationText) {
        Double baseY = originalLine.getPosition() != null ? originalLine.getPosition().getY() : null;
        if (baseY == null || continuationText == null || continuationText.isBlank()) {
            return;
        }
        double fontSize = Math.abs(originalLine.getFontSize() == null ? 12.0 : originalLine.getFontSize());
        pdf.page(PAGE_NUMBER).selectTextLinesMatching(java.util.regex.Pattern.quote(continuationText)).stream()
                .findFirst()
                .ifPresent(continuationLine -> {
                    Double continuationX = continuationLine.getPosition().getX();
                    Double continuationY = continuationLine.getPosition().getY();
                    if (continuationX != null && continuationY != null) {
                        assertTrue(continuationLine.edit()
                                .moveTo(continuationX, continuationY + (fontSize / 2))
                                .apply(), "Could not move overflow continuation line");
                    }
                });
    }
}
