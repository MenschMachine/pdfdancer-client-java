package com.pdfdancer.client.rest;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class EnvironmentInfo {
    private static final String ENV_API_TOKEN = "PDFDANCER_API_TOKEN";
    private static final String ENV_TOKEN_LEGACY = "PDFDANCER_TOKEN";

    private EnvironmentInfo() {}

    public static String envTokenOrNull() {
        String token = System.getenv(ENV_API_TOKEN);
        if (token == null || token.isBlank()) {
            token = System.getenv(ENV_TOKEN_LEGACY);
        }
        return (token == null || token.isBlank()) ? null : token;
    }

    public static String buildFingerprint() {
        try {
            String ip = getLocalIp();
            String uid = getUid();
            String osType = System.getProperty("os.name", "unknown");
            String sdkLanguage = "java";
            String timezone = getTimezone();
            String localeStr = getLocaleStr();
            String hostname = getHostname();
            String installSalt = getOrCreateSalt();
            String data = ip + uid + osType + sdkLanguage + timezone + localeStr + hostname + installSalt;
            return sha256Hex(data);
        } catch (Exception e) {
            String fallback = Optional.ofNullable(System.getProperty("user.name")).orElse("unknown")
                    + Optional.ofNullable(System.getProperty("os.name")).orElse("unknown")
                    + Optional.of(ZoneId.systemDefault().getId()).orElse("UTC");
            return sha256Hex(fallback);
        }
    }

    static String sha256Hex(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // If SHA-256 is unavailable (very unlikely), fallback to Base64 of input
            return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getUid() {
        try {
            String user = System.getProperty("user.name");
            if (user == null || user.isBlank()) user = System.getenv("USER");
            if (user == null || user.isBlank()) user = System.getenv("USERNAME");
            return (user == null || user.isBlank()) ? "unknown" : user;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getTimezone() {
        try {
            return ZoneId.systemDefault().getId();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getLocaleStr() {
        try {
            Locale loc = Locale.getDefault();
            String s = (loc == null) ? "" : loc.toString();
            return s.isBlank() ? "en_US" : s;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            String env = Optional.ofNullable(System.getenv("HOSTNAME"))
                    .orElseGet(() -> System.getenv("COMPUTERNAME"));
            return (env == null || env.isBlank()) ? "unknown" : env;
        }
    }

    private static String getOrCreateSalt() {
        try {
            Path home = Paths.get(System.getProperty("user.home"));
            Path dir = home.resolve(".pdfdancer");
            Path file = dir.resolve("fingerprint.salt");
            if (Files.exists(file)) {
                try {
                    String s = Files.readString(file).trim();
                    if (!s.isBlank()) return s;
                } catch (Exception ignored) {
                }
            }
            String salt = UUID.randomUUID().toString();
            try {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
                Files.writeString(file, salt);
            } catch (Exception ignored) {
            }
            return salt;
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
