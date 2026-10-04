package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.BoundingRect;
import com.pdfdancer.common.model.ObjectRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClippingTest extends BaseTest {
    private static final String CLIPPING_FIXTURE = "invisible-content-clipping-test.pdf";
    private static final BoundingRect TARGET_PATH_BOUNDS = new BoundingRect(260, 460, 80, 80);
    private static final BoundingRect CONTROL_PATH_BOUNDS = new BoundingRect(100, 300, 120, 80);

    private static PathReference pathWithBounds(PDFDancer pdf, BoundingRect bounds) {
        return PathTestSupport.pathWithBounds(pdf.page(1).selectPaths(), bounds);
    }

    private static void assertPathClipping(PDFDancer pdf, BoundingRect bounds, boolean clipped) {
        PDFAssertions assertions = new PDFAssertions(pdf);
        // Assertions save and reopen the PDF, which can regenerate path IDs.
        PathReference path = pathWithBounds(assertions.getPdf(), bounds);
        if (clipped) {
            assertions.assertPathHasClipping(path.getInternalId());
        } else {
            assertions.assertPathHasNoClipping(path.getInternalId());
        }
    }

    @Test
    public void clearClippingViaPathReference() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathReference path = pathWithBounds(pdf, TARGET_PATH_BOUNDS);

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, true);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);
        new PDFAssertions(pdf).assertNumberOfPaths(3, 1);

        assertTrue(path.clearClipping());

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, false);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);
        new PDFAssertions(pdf).assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearClippingViaPdfApi() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathReference path = pathWithBounds(pdf, TARGET_PATH_BOUNDS);
        ObjectRef target = pdf.selectElements().stream()
                .filter(candidate -> path.getInternalId().equals(candidate.getInternalId()))
                .findFirst()
                .orElseThrow();

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, true);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);

        assertTrue(pdf.clearClipping(target));

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, false);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);
    }

    @Test
    public void clearPathGroupClippingViaReference() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathReference path = pathWithBounds(pdf, TARGET_PATH_BOUNDS);
        PathGroupReference group = pdf.page(1).groupPaths(java.util.List.of(path.getInternalId()));

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, true);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);

        assertTrue(group.clearClipping());

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, false);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);
        new PDFAssertions(pdf).assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearPathGroupClippingViaPdfApi() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        PathReference path = pathWithBounds(pdf, TARGET_PATH_BOUNDS);
        PathGroupReference group = pdf.page(1).groupPaths(java.util.List.of(path.getInternalId()));

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, true);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);

        assertTrue(pdf.clearPathGroupClipping(group.getPageNumber(), group.getGroupId()));

        assertPathClipping(pdf, TARGET_PATH_BOUNDS, false);
        assertPathClipping(pdf, CONTROL_PATH_BOUNDS, true);
        new PDFAssertions(pdf).assertNumberOfPaths(3, 1);
    }

    @Test
    public void clearClippingViaImageReference() {
        PDFDancer pdf = createClient(CLIPPING_FIXTURE);
        ImageReference image = pdf.page(1).selectImages().get(0);

        new PDFAssertions(pdf).assertImageHasClipping(image.getInternalId());
        assertPathClipping(pdf, TARGET_PATH_BOUNDS, true);

        assertTrue(image.clearClipping());

        new PDFAssertions(pdf)
                .assertImageHasNoClipping(image.getInternalId())
                .assertImageWithIdAt(image.getInternalId(), 200, 400, 1);
        assertPathClipping(pdf, TARGET_PATH_BOUNDS, true);
    }

}
