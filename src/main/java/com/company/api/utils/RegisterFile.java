package com.company.api.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class RegisterFile {
    private final Connection connection;

    public RegisterFile(Connection connection) {
        this.connection = connection;
    }

    public void uploadFile(int userId, String filename, String path) throws IllegalArgumentException, SQLException {
        // Validar parâmetros obrigatórios
        if (userId <= 0) {
            throw new IllegalArgumentException("ID do usuário é obrigatório e deve ser maior que zero");
        }

        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo é obrigatório");
        }

        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Caminho do arquivo é obrigatório");
        }

        // Inserir no banco de dados
        try (var stmt = connection.prepareStatement("INSERT INTO files (user_id, name, path) VALUES (?, ?, ?)")) {
            stmt.setInt(1, userId);
            stmt.setString(2, filename);
            stmt.setString(3, path);
            stmt.executeUpdate();

            RegisterLog Log = new RegisterLog(connection);
            Log.create("Arquivo enviado",
                    "O arquivo " + filename + " foi enviado com sucesso",
                    userId);
        }
    }
}