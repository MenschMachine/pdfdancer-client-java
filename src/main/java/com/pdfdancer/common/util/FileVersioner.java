package com.pdfdancer.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileVersioner {

    private static Path ensureExtension(Path path, String extension) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        String fileName = path.getFileName().toString();

        // find last dot in the name
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex); // strip extension
        }

        String newFileName = fileName + extension;
        Path parent = path.getParent();
        return (parent == null) ? Path.of(newFileName) : parent.resolve(newFileName);
    }

    public static Path getNextVersion(Path directory, File baselineFile) throws IOException {
        int maxVersion = getMaxVersion(directory, baselineFile);
        int nextVersion = (maxVersion == -1) ? 0 : maxVersion + 1;

        String nextName = getVersionedName(baselineFile.getName(), nextVersion);
        return ensureExtension(directory.resolve(nextName), ".client");
    }

    public static Path getNextXmlVersion(Path directory, File baselineFile) throws IOException {
        int maxVersion = getMaxVersion(directory, baselineFile);
        int nextVersion = (maxVersion == -1) ? 0 : maxVersion + 1;

        String nextName = getVersionedName(baselineFile.getName(), nextVersion);
        return ensureExtension(directory.resolve(nextName), ".xml");
    }


    public static Path getCurrentVersion(Path directory, File baselineFile) throws IOException {
        int maxVersion = getMaxVersion(directory, baselineFile);

        if (maxVersion == -1) {
            return baselineFile.toPath();
        }

        String currentName = getVersionedName(baselineFile.getName(), maxVersion);
        return directory.resolve(currentName);
    }

    // Helper: find highest existing version (-1 if none)
    private static int getMaxVersion(Path directory, File baselineFile) throws IOException {
        if (directory == null || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Invalid directory: " + directory);
        }
        if (baselineFile == null || !baselineFile.getName().contains(".")) {
            throw new IllegalArgumentException("Invalid baseline file: " + baselineFile);
        }

        String filename = baselineFile.getName();
        int dotIndex = filename.lastIndexOf('.');
        String baseName = filename.substring(0, dotIndex);
        String extension = filename.substring(dotIndex);

        String regex = Pattern.quote(baseName) + "(?:-(\\d+))?" + Pattern.quote(extension);
        Pattern pattern = Pattern.compile(regex);

        int maxVersion = -1;

        try (Stream<Path> stream = Files.list(directory)) {
            for (Path file : (Iterable<Path>) stream::iterator) {
                String name = file.getFileName().toString();
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    if (matcher.group(1) != null) {
                        int version = Integer.parseInt(matcher.group(1));
                        maxVersion = Math.max(maxVersion, version);
                    } else {
                        // baseline exists without number
                        maxVersion = Math.max(maxVersion, 0);
                    }
                }
            }
        }

        return maxVersion;
    }

    // Helper: generate versioned filename from version number
    private static String getVersionedName(String filename, int version) {
        if (version == 0) {
            return filename; // baseline
        }

        int dotIndex = filename.lastIndexOf('.');
        String baseName = filename.substring(0, dotIndex);
        String extension = filename.substring(dotIndex);
        return baseName + "-" + version + extension;
    }
}