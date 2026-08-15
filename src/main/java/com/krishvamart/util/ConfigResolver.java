package com.krishva.krishvamart.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves configuration in the order cloud platforms expect (12-factor app
 * pattern): environment variable, then {@code config.properties} on the
 * classpath, then a hardcoded default. Environment variables win because
 * that's how Render/Railway/Fly.io/AWS/most PaaS platforms inject secrets
 * and connection strings at deploy time - the WAR is built once in CI and
 * the same artifact is configured differently per environment purely via
 * env vars, without rebuilding or committing secrets to config.properties.
 *
 * Property-file keys use dots (e.g. {@code db.url}); the matching
 * environment variable is the same name upper-cased with dots replaced by
 * underscores (e.g. {@code DB_URL}), following the usual Java/Spring
 * convention so it reads naturally on any cloud dashboard's "Environment
 * Variables" screen.
 */
public final class ConfigResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigResolver.class);

    private final Properties fileProps;

    private ConfigResolver(Properties fileProps) {
        this.fileProps = fileProps;
    }

    /** Loads config.properties from the classpath, if present (missing file is not an error - env vars may cover everything). */
    public static ConfigResolver load() {
        Properties props = new Properties();
        try (InputStream in = ConfigResolver.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                LOG.info("config.properties not found on classpath - relying on environment variables and defaults");
            }
        } catch (IOException e) {
            LOG.warn("Failed to read config.properties, relying on environment variables and defaults", e);
        }
        return new ConfigResolver(props);
    }

    /** Resolves a value: environment variable > config.properties > default. */
    public String get(String propertyKey, String defaultValue) {
        String envKey = propertyKey.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return fileProps.getProperty(propertyKey, defaultValue);
    }

    public int getInt(String propertyKey, int defaultValue) {
        String raw = get(propertyKey, null);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Invalid integer for {}: '{}' - using default {}", propertyKey, raw, defaultValue);
            return defaultValue;
        }
    }
}
