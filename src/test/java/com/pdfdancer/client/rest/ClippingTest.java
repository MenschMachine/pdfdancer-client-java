package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.ObjectRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClippingTest extends BaseTest {
    private static final String CLIPPING_FIXTURE = "invisible-content-clipping-test.pdf";
    private static final String TARGET_PATH_ID = "PATH_0_000004";
    private static final String CONTROL_PATH_ID = "PATH_0_000003";

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

        assertTrue(pdf.clearPathGroupClipping(group.getPageNumber(), group.getGroupId()));

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

}
