package com.krishva.krishvamart.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class TestDataSource {
    private TestDataSource() {
    }
    public static HikariDataSource create() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);
        HikariDataSource ds = new HikariDataSource(config);
        applySchema(ds);
        return ds;
    }

    private static void applySchema(HikariDataSource ds) {
        try (Connection conn = ds.getConnection()) {
            String script = readSchema();
            try (Statement st = conn.createStatement()) {
                for (String stmt : script.split(";")) {
                    String trimmed = stmt.strip();
                    if (!trimmed.isEmpty()) {
                        st.execute(trimmed);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize test schema", e);
        }
    }

    private static String readSchema() throws IOException {
        Path path = Path.of("db/schema.sql");
        if (Files.exists(path)) {
            return Files.readString(path);
        }
        return Files.readString(Path.of("../db/schema.sql"));
    }
}
