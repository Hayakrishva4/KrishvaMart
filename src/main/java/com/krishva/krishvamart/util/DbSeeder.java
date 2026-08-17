package com.krishva.krishvamart.util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public final class DbSeeder 
{

    private DbSeeder() 
    {}
    public static void main(String[] args) throws SQLException, IOException 
    {
        String url = System.getProperty("jdbc.url", "jdbc:h2:tcp://localhost:9092/./data/krishvamart");
        try (Connection conn = DriverManager.getConnection(url, "sa", "sa"))
         {
            runScript(conn, Path.of("db/schema.sql"));
            seedUsers(conn);
            runScript(conn, Path.of("db/seed.sql"));
            System.out.println("Seed complete against " + url);
        }
    }
    private static void seedUsers(Connection conn) throws SQLException 
    {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (var ps = conn.prepareStatement(sql)) 
        {
            insert(ps, "Admin", "admin@krishvamart.com", "Admin@12345", "ADMIN");
            insert(ps, "Priya Seller", "priya.seller@krishvamart.com", "Seller@123", "SELLER");
            insert(ps, "Arjun Seller", "arjun.seller@krishvamart.com", "Seller@123", "SELLER");
            insert(ps, "Divya Buyer", "divya.buyer@krishvamart.com", "Buyer@1234", "BUYER");
            insert(ps, "Karthik Buyer", "karthik.buyer@krishvamart.com", "Buyer@1234", "BUYER");
        }
        System.out.println("Seeded users. Demo passwords (change/remove before any real deployment):");
        System.out.println("  admin@krishvamart.com / Admin@12345");
        System.out.println("  priya.seller@krishvamart.com / Seller@123 (seller id 2, used by seed.sql)");
        System.out.println("  arjun.seller@krishvamart.com / Seller@123 (seller id 3, used by seed.sql)");
        System.out.println("  divya.buyer@krishvamart.com / Buyer@1234");
    }
   private static void insert(java.sql.PreparedStatement ps, String name, String email,
    String plainPassword, String role) throws SQLException 
    {
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, PasswordUtil.hash(plainPassword));
        ps.setString(4, role);
        ps.executeUpdate();
    }
    private static void runScript(Connection conn, Path path) throws IOException, SQLException 
    {
        String script = Files.readString(path);
        try (Statement st = conn.createStatement()) 
        {
            for (String stmt : script.split(";")) 
            {
                String trimmed = stmt.strip();
                if (!trimmed.isEmpty()) 
                {
                    st.execute(trimmed);
                }
            }
        }
    }
}
