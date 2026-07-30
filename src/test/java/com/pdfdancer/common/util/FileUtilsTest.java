package com.pdfdancer.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

    @TempDir
    Path tempDirectory;

    @Test
    void writesToBareFilenameWithoutRequiringAParentDirectory() throws Exception {
        byte[] data = "bare filename".getBytes();
        Path output = Path.of("file-utils-test-" + UUID.randomUUID() + ".pdf");

        try {
            assertEquals(output.toFile(), FileUtils.writeBytesToFile(data, output.toString()));
            assertArrayEquals(data, Files.readAllBytes(output));
        } finally {
            Files.deleteIfExists(output);
        }
    }

    @Test
    void createsMissingParentDirectories() throws Exception {
        byte[] data = "nested filename".getBytes();
        Path output = tempDirectory.resolve("missing").resolve("parent").resolve("output.pdf");

        assertEquals(output.toFile(), FileUtils.writeBytesToFile(data, output.toString()));
        assertArrayEquals(data, Files.readAllBytes(output));
    }
}
