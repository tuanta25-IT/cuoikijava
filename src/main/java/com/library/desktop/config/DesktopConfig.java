package com.library.desktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DesktopConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DesktopConfig.class.getClassLoader().getResourceAsStream("desktop.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không thể tải desktop.properties", e);
        }
    }

    private DesktopConfig() {
    }

    public static String get(String key) {
        String fromEnv = System.getenv(toEnvKey(key));
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        String value = PROPS.getProperty(key, "");
        if (value.startsWith("${") && value.endsWith("}")) {
            return resolveExpression(value);
        }
        return value;
    }

    private static String resolveExpression(String value) {
        String body = value.substring(2, value.length() - 1);
        int idx = body.indexOf(':');
        if (idx < 0) {
            String envValue = System.getenv(body);
            return envValue == null ? "" : envValue;
        }

        String envKey = body.substring(0, idx);
        String fallback = body.substring(idx + 1);
        String envValue = System.getenv(envKey);
        if (envValue == null || envValue.isBlank()) {
            return fallback;
        }
        return envValue;
    }

    private static String toEnvKey(String key) {
        return switch (key) {
            case "db.host" -> "DB_HOST";
            case "db.port" -> "DB_PORT";
            case "db.name" -> "DB_NAME";
            case "db.user" -> "DB_USERNAME";
            case "db.password" -> "DB_PASSWORD";
            default -> key.toUpperCase().replace('.', '_');
        };
    }
}
