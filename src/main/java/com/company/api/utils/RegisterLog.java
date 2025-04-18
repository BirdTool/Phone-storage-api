package com.company.api.utils;

import com.company.api.database.Database;
import java.sql.Connection;
import java.sql.SQLException;

public class RegisterLog {
    private final Connection connection;
    private boolean useExternalConnection;

    public RegisterLog() {
        this.connection = null; // Será criada quando necessário
        this.useExternalConnection = false;
    }

    public RegisterLog(Connection existingConnection) {
        this.connection = existingConnection;
        this.useExternalConnection = true;
    }

    public void create(String title, int userId) throws SQLException, IllegalArgumentException {
        create(title, null, userId);
    }

    public void create(String title, String description, int userId) throws SQLException, IllegalArgumentException {
        // Validação dos campos obrigatórios
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Título do log é obrigatório");
        }

        if (userId <= 0) {
            throw new IllegalArgumentException("ID do usuário é obrigatório e deve ser maior que zero");
        }

        // Decide se usa conexão existente ou cria uma nova
        if (useExternalConnection) {
            insertLog(connection, title, description, userId);
        } else {
            try (Connection newConnection = Database.connect()) {
                insertLog(newConnection, title, description, userId);
            }
        }
    }

    private void insertLog(Connection conn, String title, String description, int userId) throws SQLException {
        if (description == null || description.trim().isEmpty()) {
            try (var stmt = conn.prepareStatement("INSERT INTO logs (title, user_id) VALUES (?, ?)")) {
                stmt.setString(1, title);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } else {
            try (var stmt = conn.prepareStatement("INSERT INTO logs (title, description, user_id) VALUES (?, ?, ?)")) {
                stmt.setString(1, title);
                stmt.setString(2, description);
                stmt.setInt(3, userId);
                stmt.executeUpdate();
            }
        }
    }
}