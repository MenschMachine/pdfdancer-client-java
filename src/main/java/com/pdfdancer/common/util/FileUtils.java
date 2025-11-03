package com.pdfdancer.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utility class providing file system operations for the PDF processing API.
 * This class offers convenient methods for writing binary data to files and
 * creating temporary files for PDF processing operations, with proper directory
 * creation and resource management.
 */
public class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Writes binary data to a file at the specified path.
     * This method creates parent directories if they don't exist and safely
     * writes the provided data to the target file using proper resource management.
     *
     * @param data     binary data to write to the file
     * @param filePath target file path where data should be written
     * @return the File object representing the written file
     * @throws IOException if file creation or writing fails
     */
    public static File writeBytesToFile(byte[] data, String filePath) throws IOException {
        File file = new File(filePath);

        // Make sure parent directories exist
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }

        return file;
    }

    /**
     * Saves PDF data to a temporary file with automatic cleanup.
     * This method creates a temporary file with the specified base filename,
     * writes the PDF data to it, and sets it for automatic deletion when
     * the application exits. Useful for processing uploaded PDF files.
     *
     * @param pdfData     binary PDF file data to save
     * @param pdfFilename base filename for the temporary file (without extension)
     * @return Path to the created temporary PDF file
     * @throws IOException if temporary file creation or writing fails
     */
    public static Path saveToFile(byte[] pdfData, Path sessionDir, String pdfFilename) throws IOException {

        Path filePath = sessionDir.resolve(pdfFilename);
        LOG.info("Saving PDF data to temporary file: {}", filePath);
        Files.write(filePath, pdfData, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        filePath.toFile().deleteOnExit();
        return filePath;
    }

    public static byte[] bufferedImageToBytes(BufferedImage image, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, format, baos);  // format = "png", "jpg", etc.
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public static void copyFiles(String sourceDirName, String targetDirName, String globPattern) throws IOException {
        Path sourceDir = Paths.get(sourceDirName);   // Source directory
        Path targetDir = Paths.get(targetDirName);   // Target directory

        // Create target directory if it doesn't exist
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Get a PathMatcher for the glob pattern
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);


        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Make the path relative to sourceDir for matching
                Path relativePath = sourceDir.relativize(file);

                if (matcher.matches(relativePath)) {
                    Path targetFile = targetDir.resolve(relativePath);
                    Files.createDirectories(targetFile.getParent());
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Copied: " + file + " -> " + targetFile);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}