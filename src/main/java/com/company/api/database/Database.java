package com.company.api.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Database {
    private static final String DB_PATH = "src/main/resources/database.sqlite";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
    }

    public static void initialize() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            String sql = new String(Files.readAllBytes(Paths.get("src/main/java/com/company/api/database/migrations.sql")));
            stmt.executeUpdate(sql);
            System.out.println("✅ Banco de dados inicializado.");

        } catch (Exception e) {
            System.out.println("❌ Erro ao inicializar o banco de dados:");
            e.printStackTrace();
        }
    }
}
