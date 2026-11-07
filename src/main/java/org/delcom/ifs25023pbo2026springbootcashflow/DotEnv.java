package org.delcom.ifs25023pbo2026springbootcashflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Memuat berkas {@code .env} (format KEY=VALUE) ke System properties
 * sebelum Spring Boot dijalankan, sehingga placeholder seperti
 * {@code ${APP_PORT:8080}} pada application.yaml dapat terisi.
 * Nilai yang sudah ada di System properties / environment tidak ditimpa.
 */
public final class DotEnv {
    private static final String DEFAULT_FILE = ".env";

    private DotEnv() {
    }

    /** Memuat {@code .env} dari direktori kerja saat ini (diabaikan jika tidak ada). */
    public static void load() {
        load(Path.of(DEFAULT_FILE));
    }

    /** Memuat berkas env dari path tertentu (diabaikan jika tidak ada). */
    public static void load(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return;
        }
        for (String raw : lines) {
            parseLine(raw);
        }
    }

    private static void parseLine(String raw) {
        String line = raw.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        int separator = line.indexOf('=');
        if (separator <= 0) {
            return;
        }
        String key = line.substring(0, separator).trim();
        String value = stripQuotes(line.substring(separator + 1).trim());
        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
