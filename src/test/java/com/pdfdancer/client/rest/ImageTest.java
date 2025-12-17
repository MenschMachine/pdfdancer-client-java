package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Image;
import com.pdfdancer.common.model.Size;
import com.pdfdancer.common.request.ImageTransformRequest.FlipDirection;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for image operations using basic-image-test.pdf which contains:
 * - 1 page
 * - 3 images on page 1:
 *   - IMAGE_000001 at (50, 600) size 100x100
 *   - IMAGE_000002 at (200, 600) size 150x100
 *   - IMAGE_000003 at (400, 600) size 100x150
 */
public class ImageTest extends BaseTest {

    @Override
    protected String getPdfFile() {
        return "basic-image-test.pdf";
    }

    @Test
    public void findImages() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.selectImages();
        assertEquals(3, images.size());

        List<ImageReference> page1Images = client.page(1).selectImages();
        assertEquals(3, page1Images.size());
    }

    @Test
    public void findImageByPosition() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.page(1).selectImagesAt(50, 600, 1);
        assertEquals(1, images.size());
        assertEquals("IMAGE_000001", images.get(0).getInternalId());

        List<ImageReference> noImages = client.page(1).selectImagesAt(0, 0, 1);
        assertEquals(0, noImages.size());
    }

    @Test
    public void findSingularImageByPosition() {
        PDFDancer client = createClient();

        Optional<ImageReference> image = client.page(1).selectImageAt(200, 600, 1);
        assertTrue(image.isPresent());
        assertEquals("IMAGE_000002", image.get().getInternalId());
        assertEquals(150, image.get().getWidth(), 0.01);
        assertEquals(100, image.get().getHeight(), 0.01);

        Optional<ImageReference> empty = client.page(1).selectImageAt(0, 0, 1);
        assertFalse(empty.isPresent());
    }

    @Test
    public void getImageDimensions() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.page(1).selectImages();

        // IMAGE_000001: 100x100 (square)
        ImageReference img1 = images.stream()
                .filter(i -> i.getInternalId().equals("IMAGE_000001"))
                .findFirst().orElseThrow();
        assertEquals(100, img1.getWidth(), 0.01);
        assertEquals(100, img1.getHeight(), 0.01);
        assertEquals(1.0, img1.getAspectRatio(), 0.01);

        // IMAGE_000002: 150x100 (landscape)
        ImageReference img2 = images.stream()
                .filter(i -> i.getInternalId().equals("IMAGE_000002"))
                .findFirst().orElseThrow();
        assertEquals(150, img2.getWidth(), 0.01);
        assertEquals(100, img2.getHeight(), 0.01);
        assertEquals(1.5, img2.getAspectRatio(), 0.01);

        // IMAGE_000003: 100x150 (portrait)
        ImageReference img3 = images.stream()
                .filter(i -> i.getInternalId().equals("IMAGE_000003"))
                .findFirst().orElseThrow();
        assertEquals(100, img3.getWidth(), 0.01);
        assertEquals(150, img3.getHeight(), 0.01);
        assertEquals(0.666, img3.getAspectRatio(), 0.01);
    }

    @Test
    public void deleteImage() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.selectImages();
        assertEquals(3, images.size());

        assertTrue(images.get(0).delete());

        assertEquals(2, client.selectImages().size());

        new PDFAssertions(client)
                .assertNumberOfImages(2, 1);
    }

    @Test
    public void deleteAllImages() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.selectImages();
        assertEquals(3, images.size());

        for (ImageReference image : images) {
            assertTrue(image.delete());
        }

        assertEquals(0, client.selectImages().size());

        new PDFAssertions(client)
                .assertNumberOfImages(0, 1);
    }

    @Test
    public void moveImage() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);
        assertEquals(50, image.getPosition().getX(), 0.01);
        assertEquals(600, image.getPosition().getY(), 0.01);

        assertTrue(image.moveTo(100, 500));

        List<ImageReference> movedImages = client.page(1).selectImagesAt(100, 500, 1);
        assertEquals(1, movedImages.size());
        assertEquals(100, movedImages.get(0).getPosition().getX(), 0.01);
        assertEquals(500, movedImages.get(0).getPosition().getY(), 0.01);
    }

    @Test
    public void addImage() throws IOException {
        PDFDancer client = createClient();

        assertEquals(3, client.selectImages().size());

        File file = new File("src/test/resources/fixtures/logo-80.png");
        assertTrue(client.newImage().fromFile(file).at(1, 300, 300).add());

        assertEquals(4, client.selectImages().size());

        new PDFAssertions(client)
                .assertNumberOfImages(4, 1)
                .assertImageAt(300, 300, 1);
    }

    // ===========================
    // Image Transform Tests
    // ===========================

    @Test
    public void replaceImage() throws IOException {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        File replacementFile = new File("src/test/resources/fixtures/logo-80.png");
        assertTrue(image.replace(replacementFile));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageAt(50, 600, 1);
    }

    @Test
    public void replaceImageWithImageObject() throws IOException {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        File replacementFile = new File("src/test/resources/fixtures/logo-80.png");
        Image newImage = Image.fromFile(replacementFile);

        assertTrue(image.replace(newImage));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1);
    }

    @Test
    public void scaleImage() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);
        assertEquals(100, image.getWidth(), 0.01);
        assertEquals(100, image.getHeight(), 0.01);

        assertTrue(image.scale(0.5));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 50, 50, 5)
                .assertImageAspectRatio(50, 600, 1, 1.0, 0.1);
    }

    @Test
    public void scaleImageToSize() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        assertTrue(image.scaleTo(200, 150));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 200, 150, 5);
    }

    @Test
    public void scaleImagePreserveAspectRatio() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(200, 600, 1).get(0);
        double originalAspectRatio = image.getAspectRatio();
        assertEquals(1.5, originalAspectRatio, 0.01);

        assertTrue(image.scaleTo(new Size(300, 300), true));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageAspectRatio(200, 600, 1, originalAspectRatio, 0.1);
    }

    @Test
    public void rotateImage() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        assertTrue(image.rotate(45));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageAt(50, 600, 1);
    }

    @Test
    public void rotateImage90Degrees() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(200, 600, 1).get(0);
        assertEquals(150, image.getWidth(), 0.01);
        assertEquals(100, image.getHeight(), 0.01);

        assertTrue(image.rotate(90));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(200, 600, 1, 100, 150, 5);
    }

    @Test
    public void cropImage() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);
        assertEquals(100, image.getWidth(), 0.01);
        assertEquals(100, image.getHeight(), 0.01);

        assertTrue(image.crop(10, 10, 10, 10));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 80, 80, 5);
    }

    @Test
    public void setImageOpacity() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        assertTrue(image.opacity(0.5));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 100, 100, 5);
    }

    @Test
    public void flipImageHorizontal() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        assertTrue(image.flipHorizontal());

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 100, 100, 5);
    }

    @Test
    public void flipImageVertical() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        assertTrue(image.flipVertical());

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 100, 100, 5);
    }

    @Test
    public void flipImageBoth() {
        PDFDancer client = createClient();

        ImageReference image = client.page(1).selectImagesAt(50, 600, 1).get(0);

        assertTrue(image.flip(FlipDirection.BOTH));

        new PDFAssertions(client)
                .assertNumberOfImages(3, 1)
                .assertImageSize(50, 600, 1, 100, 100, 5);
    }
}
