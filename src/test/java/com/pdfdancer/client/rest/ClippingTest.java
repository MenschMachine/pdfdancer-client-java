package com.pdfdancer.client.rest;


import com.pdfdancer.common.model.ObjectRef;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClippingTest extends BaseTest {
    private static final String CLIPPING_FIXTURE = "clipping/invisible-content-clipping-test.pdf";
    private static final String TARGET_PATH_ID = "PATH_0_000004";
    private static final String CONTROL_PATH_ID = "PATH_0_000003";

    @Override
    protected String getPdfFile() {
        return CLIPPING_FIXTURE;
    }

    @Test
    public void clearClippingViaPathReference() {
        PDFDancer pdf = createClient();
        PathReference path = findPathById(pdf, TARGET_PATH_ID);

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1)
                .assertNumberOfPaths(3, 1);

        assertTrue(path.clearClipping());

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1)
                .assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearClippingViaPdfApi() {
        PDFDancer pdf = createClient();

        ObjectRef pathRef = pdf.getPageSnapshot(1).elements().stream()
                .filter(ref -> TARGET_PATH_ID.equals(ref.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Path " + TARGET_PATH_ID + " not found"));

        new PDFAssertions(pdf).assertPathHasClipping(TARGET_PATH_ID, 1);

        assertTrue(pdf.clearClipping(pathRef));

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1);
    }

    @Test
    public void clearPathGroupClippingViaReference() {
        PDFDancer pdf = createClient();

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1);

        PathGroupReference group = pdf.page(1).groupPaths(List.of(TARGET_PATH_ID));
        assertNotNull(group.getGroupId());
        assertTrue(group.clearClipping());

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1)
                .assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearPathGroupClippingViaPdfApi() {
        PDFDancer pdf = createClient();

        new PDFAssertions(pdf)
                .assertPathHasClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1);

        PathGroupReference group = pdf.page(1).groupPaths(List.of(TARGET_PATH_ID));
        assertNotNull(group.getGroupId());
        assertTrue(pdf.clearPathGroupClipping(1, group.getGroupId()));

        new PDFAssertions(pdf)
                .assertPathHasNoClipping(TARGET_PATH_ID, 1)
                .assertPathHasClipping(CONTROL_PATH_ID, 1)
                .assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearClippingViaImageReference() {
        PDFDancer pdf = createClient();
        ImageReference image = pdf.page(1).selectImages().get(0);
        double x = image.getPosition().getX();
        double y = image.getPosition().getY();

        new PDFAssertions(pdf)
                .assertImageHasClipping(image.getInternalId(), 1)
                .assertPathHasClipping(TARGET_PATH_ID, 1);

        assertTrue(image.clearClipping());

        new PDFAssertions(pdf)
                .assertImageHasNoClipping(image.getInternalId(), 1)
                .assertPathHasClipping(TARGET_PATH_ID, 1)
                .assertImageWithIdAt(image.getInternalId(), x, y, 1);
    }

    @Test
    public void clearClippingViaTextLineReference() {
        PDFDancer pdf = createClient("ObviouslyAwesome.pdf");
        List<TextLineReference> lines = pdf.page(1).selectTextLines();
        assertTrue(lines.size() > 0, "Expected at least one text line in fixture");
        int before = lines.size();

        assertTrue(lines.get(0).clearClipping());

        int after = pdf.page(1).selectTextLines().size();
        assertEquals(before, after, "Clearing clipping should not remove text lines");
    }

    private PathReference findPathById(PDFDancer pdf, String internalId) {
        return pdf.page(1).selectPaths().stream()
                .filter(path -> internalId.equals(path.getInternalId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Path " + internalId + " not found"));
    }
}
