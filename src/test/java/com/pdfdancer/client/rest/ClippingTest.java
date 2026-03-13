package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClippingTest extends BaseTest {
    private static final String CLIPPING_FIXTURE = "invisible-content-clipping-test.pdf";
    private static final String TARGET_PATH_ID = "PATH_0_000004";
    private static final String CONTROL_PATH_ID = "PATH_0_000003";
    private static final String CLIPPED_TEXT = "Clipped endstream endobj text line";
    private static final String CONTROL_CLIPPED_TEXT = "Control clipped text line";
    private static final String MULTI_STREAM_CLIPPED_TEXT = "Clipped text line from second stream";
    private static final String MULTI_STREAM_CONTROL_TEXT = "Control text line from second stream";

    @Test
    public void clearClippingViaPathReference() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathReference path = pdf.page(1).selectPaths().stream()
                .filter(candidate -> TARGET_PATH_ID.equals(candidate.getInternalId()))
                .findFirst()
                .orElseThrow();

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID)
                .assertNumberOfPaths(3, 1);

        assertTrue(path.clearClipping());

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID)
                .assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearClippingViaPdfApi() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        ObjectRef target = pdf.selectElements().stream()
                .filter(candidate -> TARGET_PATH_ID.equals(candidate.getInternalId()))
                .findFirst()
                .orElseThrow();

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID);

        assertTrue(pdf.clearClipping(target));

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID);
    }

    @Test
    public void clearPathGroupClippingViaReference() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathGroupReference group = pdf.page(1).groupPaths(java.util.List.of(TARGET_PATH_ID));

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID);

        assertTrue(group.clearClipping());

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID)
                .assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearPathGroupClippingViaPdfApi() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathGroupReference group = pdf.page(1).groupPaths(java.util.List.of(TARGET_PATH_ID));

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID);

        assertTrue(pdf.clearPathGroupClipping(group.getPageIndex(), group.getGroupId()));

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID)
                .assertPathHasClipping(CONTROL_PATH_ID)
                .assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearClippingViaImageReference() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        ImageReference image = pdf.page(1).selectImages().get(0);

        new PDFAssertions(pdf)
                .assertImageHasClipping(image.getInternalId())
                .assertPathHasClipping(TARGET_PATH_ID);

        assertTrue(image.clearClipping());

        new PDFAssertions(pdf)
                .assertImageHasNoClipping(image.getInternalId())
                .assertPathHasClipping(TARGET_PATH_ID)
                .assertImageWithIdAt(image.getInternalId(), 200, 400, 1);
    }

    @Test
    public void clearClippingViaTextLineReference() {
        PDFDancer pdf = PDFDancer.createSession(BaseTest.getValidToken(), createClippedTextPdf(), BaseTest.httpClient);
        TextLineReference line = pdf.page(1).selectTextLinesStartingWith(CLIPPED_TEXT).get(0);

        new PDFAssertions(pdf)
                .assertTextlineHasClipping(CLIPPED_TEXT)
                .assertTextlineHasClipping(CONTROL_CLIPPED_TEXT)
                .assertTextlineExists(CLIPPED_TEXT, 1)
                .assertTextlineExists(CONTROL_CLIPPED_TEXT, 1);

        assertTrue(line.clearClipping());

        new PDFAssertions(pdf)
                .assertTextlineHasNoClipping(CLIPPED_TEXT)
                .assertTextlineHasClipping(CONTROL_CLIPPED_TEXT)
                .assertTextlineExists(CLIPPED_TEXT, 1)
                .assertTextlineExists(CONTROL_CLIPPED_TEXT, 1);
    }

    @Test
    public void clearClippingViaParagraphReference() {
        PDFDancer pdf = PDFDancer.createSession(BaseTest.getValidToken(), createClippedTextPdf(), BaseTest.httpClient);
        TextParagraphReference paragraph = pdf.page(1).selectParagraphsStartingWith(CLIPPED_TEXT).get(0);

        new PDFAssertions(pdf)
                .assertParagraphHasClipping(CLIPPED_TEXT)
                .assertParagraphHasClipping(CONTROL_CLIPPED_TEXT)
                .assertParagraphExists(CLIPPED_TEXT, 1)
                .assertParagraphExists(CONTROL_CLIPPED_TEXT, 1);

        assertTrue(paragraph.clearClipping());

        new PDFAssertions(pdf)
                .assertParagraphHasNoClipping(CLIPPED_TEXT)
                .assertParagraphHasClipping(CONTROL_CLIPPED_TEXT)
                .assertParagraphExists(CLIPPED_TEXT, 1)
                .assertParagraphExists(CONTROL_CLIPPED_TEXT, 1)
                .assertTextlineExists(CLIPPED_TEXT, 1)
                .assertTextlineExists(CONTROL_CLIPPED_TEXT, 1);
    }

    @Test
    public void detectsClippingAcrossMultipleContentStreams() {
        PDFDancer pdf = PDFDancer.createSession(BaseTest.getValidToken(), createMultiStreamClippedTextPdf(), BaseTest.httpClient);
        TextLineReference line = pdf.page(1).selectTextLinesStartingWith(MULTI_STREAM_CLIPPED_TEXT).get(0);

        new PDFAssertions(pdf)
                .assertTextlineHasClipping(MULTI_STREAM_CLIPPED_TEXT)
                .assertTextlineHasClipping(MULTI_STREAM_CONTROL_TEXT)
                .assertTextlineExists(MULTI_STREAM_CLIPPED_TEXT, 1)
                .assertTextlineExists(MULTI_STREAM_CONTROL_TEXT, 1);

        assertTrue(line.clearClipping());

        new PDFAssertions(pdf)
                .assertTextlineHasNoClipping(MULTI_STREAM_CLIPPED_TEXT)
                .assertTextlineHasClipping(MULTI_STREAM_CONTROL_TEXT)
                .assertTextlineExists(MULTI_STREAM_CLIPPED_TEXT, 1)
                .assertTextlineExists(MULTI_STREAM_CONTROL_TEXT, 1);
    }

    private static byte[] createClippedTextPdf() {
        String clippedTextStream = String.join("\n", buildClippedTextSection(new String[][]{
                {CLIPPED_TEXT, "200", "400"},
                {CONTROL_CLIPPED_TEXT, "200", "260"}
        }));
        return createPdfWithContentStreams(java.util.List.of(clippedTextStream));
    }

    private static byte[] createMultiStreamClippedTextPdf() {
        String clippingSetupStream = String.join("\n",
                "q",
                "0 0 50 50 re",
                "W n",
                "");
        java.util.List<String> clippedTextSection = buildClippedTextSection(new String[][]{
                {MULTI_STREAM_CLIPPED_TEXT, "200", "400"},
                {MULTI_STREAM_CONTROL_TEXT, "200", "360"}
        });
        String clippedTextStream = String.join("\n", clippedTextSection.subList(3, clippedTextSection.size()));
        return createPdfWithContentStreams(java.util.List.of(clippingSetupStream, clippedTextStream));
    }

    private static java.util.List<String> buildClippedTextSection(String[][] texts) {
        java.util.List<String> content = new java.util.ArrayList<>();
        content.add("q");
        content.add("0 0 50 50 re");
        content.add("W n");
        for (String[] text : texts) {
            content.add("BT");
            content.add("/F1 24 Tf");
            content.add(String.format("1 0 0 1 %s %s Tm", text[1], text[2]));
            content.add(String.format("(%s) Tj", text[0]));
            content.add("ET");
        }
        content.add("Q");
        content.add("");
        return content;
    }

    private static byte[] createPdfWithContentStreams(java.util.List<String> contentStreams) {
        java.util.List<Integer> contentObjectIds = new java.util.ArrayList<>();
        for (int i = 0; i < contentStreams.size(); i++) {
            contentObjectIds.add(5 + i);
        }

        String contentsEntry;
        if (contentObjectIds.size() == 1) {
            contentsEntry = contentObjectIds.get(0) + " 0 R";
        } else {
            String refs = contentObjectIds.stream()
                    .map(objectId -> objectId + " 0 R")
                    .collect(java.util.stream.Collectors.joining(" "));
            contentsEntry = "[" + refs + "]";
        }

        java.util.List<Object[]> objects = new java.util.ArrayList<>();
        objects.add(new Object[]{1, "<< /Type /Catalog /Pages 2 0 R >>"});
        objects.add(new Object[]{2, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>"});
        objects.add(new Object[]{3, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents " + contentsEntry + " >>"});
        objects.add(new Object[]{4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"});

        for (int i = 0; i < contentStreams.size(); i++) {
            String contentStream = contentStreams.get(i);
            int objectId = contentObjectIds.get(i);
            int length = contentStream.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1).length;
            objects.add(new Object[]{objectId, "<< /Length " + length + " >>\nstream\n" + contentStream + "endstream"});
        }

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        java.util.Map<Integer, Integer> offsets = new java.util.LinkedHashMap<>();
        offsets.put(0, 0);
        for (Object[] object : objects) {
            int objectId = (Integer) object[0];
            String body = (String) object[1];
            offsets.put(objectId, pdf.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1).length);
            pdf.append(objectId).append(" 0 obj\n").append(body).append("\nendobj\n");
        }

        int xrefOffset = pdf.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Object[] object : objects) {
            int objectId = (Integer) object[0];
            pdf.append(String.format("%010d 00000 n \n", offsets.get(objectId)));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");
        return pdf.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }
}
