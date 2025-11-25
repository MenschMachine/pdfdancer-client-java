package com.pdfdancer.client.rest;

import com.pdfdancer.common.model.Position;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTest extends BaseTest {

    @Test
    public void findImages() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.selectImages(); // all images
        assertEquals(3, images.size());

        List<ImageReference> firstPageImages = client.page(1).selectImages();
        assertEquals(2, firstPageImages.size());
    }

    @Test
    public void deleteImage() {
        PDFDancer client = createClient();

        List<ImageReference> images = client.selectImages();
        for (ImageReference image : images) {
            assertDoesNotThrow(image::delete, "could not delete image " + image.getInternalId());
        }

        List<ImageReference> afterDelete = client.selectImages();
        assertEquals(0, afterDelete.size());

        byte[] bytes = client.getFileBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        client.save("/tmp/deleteImage.pdf");
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

        PDFAssertions assertions = new PDFAssertions(client);
        for (int i = 0; i < client.getPages().size(); i++) {
            assertions.assertNumberOfImages(0, i+1);
        }
    }


    @Test
    public void moveImage() {

        PDFDancer client = createClient();

        List<ImageReference> images = client.selectImages();
        ImageReference image = images.get(2);

        Position pos = image.getPosition();
        assertEquals(54, pos.getX());
        assertEquals(300, pos.getY().intValue());

        assertTrue(image.moveTo(50.1, 100));

        images = client.selectImages();
        image = images.get(2);

        pos = image.getPosition();
        assertEquals(50.1, pos.getX(), 0.01);
        assertEquals(100, pos.getY(), 0.01);
    }

    @Test
    public void findImageByPosition() {
        PDFDancer client = createClient();
        List<ImageReference> images = client.page(12).selectImagesAt(0, 0);
        assertEquals(0, images.size());
        images = client.page(12).selectImagesAt(54, 300, 1);
        assertEquals(1, images.size());
        assertEquals("IMAGE_000003", images.get(0).getInternalId());
    }

    @Test
    public void findSingularImageByPosition() {
        PDFDancer client = createClient();

        // Test finding a single image at a known position with sufficient epsilon
        Optional<ImageReference> image = client.page(12).selectImageAt(54, 300, 1);
        assertTrue(image.isPresent(), "Should find image at known position");
        assertEquals("IMAGE_000003", image.get().getInternalId());
        assertEquals(54, image.get().getPosition().getX().intValue());
        assertEquals(300, image.get().getPosition().getY().intValue());

        // Test at position with no image
        Optional<ImageReference> emptyResult = client.page(12).selectImageAt(0, 0, 1);
        assertFalse(emptyResult.isPresent(), "Should return empty Optional when no image found");
    }

    @Test
    public void addImage() throws IOException {
        PDFDancer client = createClient();

        List<ImageReference> before = client.selectImages();
        assertEquals(3, before.size());

        File file = new File("src/test/resources/fixtures/logo-80.png");

        assertTrue(
                client.newImage()
                        .fromFile(file)
                        .at(7, 50.1, 98)
                        .add()
        );

        List<ImageReference> after = client.selectImages();
        assertEquals(4, after.size());

        List<ImageReference> page7 = client.page(7).selectImages();
        assertEquals(1, page7.size());

        ImageReference img = page7.get(0);
        assertEquals(7, img.getPosition().getPageNumber());
        assertEquals("IMAGE_000004", img.getInternalId());
        assertEquals(50.1, img.getPosition().getX(), 0.01);
        assertEquals(98, img.getPosition().getY(), 0.01);
    }

    @Test
    public void replaceImage() throws IOException {
        // TODO Prio B
        PDFDancer client = createClient();
    }

    @Test
    public void scaleImage() throws IOException {
        // TODO Prio C
        PDFDancer client = createClient();
    }

    @Test
    public void cropImage() throws IOException {
        // TODO Prio C
        PDFDancer client = createClient();
    }
}
