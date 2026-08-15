package com.krishva.krishvamart.listener;

import com.krishva.krishvamart.util.ConfigResolver;
import com.krishva.krishvamart.util.PasswordUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Self-initializes a fresh database on first boot: applies {@code
 * db/schema.sql} (bundled on the classpath by the Maven build, see pom.xml),
 * then seeds the demo admin/seller/buyer accounts and sample products from
 * {@code db/seed.sql} if the {@code users} table is empty.
 *
 * This exists specifically for cloud deployment: on a platform like Render
 * or Railway there's no shell to SSH into and run {@code mvn exec:java}
 * against {@link com.krishva.krishvamart.util.DbSeeder} - the container just
 * starts and needs to be immediately usable. schema.sql's {@code CREATE
 * TABLE IF NOT EXISTS} / {@code ADD COLUMN IF NOT EXISTS} statements make
 * re-running it on every restart safe; the seed step only ever runs once
 * (guarded by an empty-users check) so redeploys never duplicate demo data
 * or overwrite real data with a live dataset.
 *
 * Executing a bundled, version-controlled SQL script via plain {@link
 * Statement} here is a deliberate, narrow exception to Section 2 Rule 1
 * (PreparedStatement only) - that rule targets queries built from *request*
 * input; this script is a trusted, static file shipped inside the WAR, the
 * same category of exception Flyway/Liquibase make internally for migration
 * scripts. No user input ever reaches this class.
 *
 * Disable entirely by setting AUTO_INIT_SCHEMA=false (e.g. against a
 * production database you manage migrations for separately).
 */
public final class SchemaInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaInitializer.class);

    private SchemaInitializer() {
    }

    public static void run(DataSource dataSource, ConfigResolver config) {
        if (!"true".equalsIgnoreCase(config.get("auto.init.schema", "true"))) {
            LOG.info("AUTO_INIT_SCHEMA=false - skipping schema self-initialization");
            return;
        }
        try (Connection conn = dataSource.getConnection()) {
            applyScript(conn, "db/schema.sql");
            if (isUsersTableEmpty(conn)) {
                LOG.info("users table is empty - seeding demo accounts and sample products");
                seedDemoUsers(conn);
                applyScript(conn, "db/seed.sql");
            } else {
                LOG.info("users table already has data - skipping demo seed");
            }
        } catch (SQLException | IOException e) {
            LOG.error("Schema self-initialization failed - the app may not function correctly", e);
        }
    }

    private static boolean isUsersTableEmpty(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    private static void seedDemoUsers(Connection conn) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertUser(ps, "Admin", "admin@krishvamart.com", "Admin@12345", "ADMIN");
            insertUser(ps, "Priya Seller", "priya.seller@krishvamart.com", "Seller@123", "SELLER");
            insertUser(ps, "Arjun Seller", "arjun.seller@krishvamart.com", "Seller@123", "SELLER");
            insertUser(ps, "Divya Buyer", "divya.buyer@krishvamart.com", "Buyer@1234", "BUYER");
            insertUser(ps, "Karthik Buyer", "karthik.buyer@krishvamart.com", "Buyer@1234", "BUYER");
        }
        LOG.info("Seeded demo accounts (change/remove before any real deployment): "
                + "admin@krishvamart.com / priya.seller@krishvamart.com / divya.buyer@krishvamart.com "
                + "- see README for full credential list");
    }

    private static void insertUser(PreparedStatement ps, String name, String email, String plainPassword, String role)
            throws SQLException {
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, PasswordUtil.hash(plainPassword));
        ps.setString(4, role);
        ps.executeUpdate();
    }

    private static void applyScript(Connection conn, String classpathPath) throws IOException, SQLException {
        String script = readClasspathResource(classpathPath);
        try (Statement st = conn.createStatement()) {
            for (String stmt : script.split(";")) {
                String trimmed = stmt.strip();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        }
    }

    private static String readClasspathResource(String path) throws IOException {
        try (InputStream in = SchemaInitializer.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Classpath resource not found: " + path
                        + " (expected to be bundled from db/ by the Maven build - see pom.xml resources)");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        }
    }
}
