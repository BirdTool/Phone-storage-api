package com.company.api.controllers.storageManager.controllers;

import com.company.api.database.Database;
import com.company.api.utils.RegisterLog;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

public class getContentFile {
    public static String execute(spark.Request req, spark.Response res) throws SQLException {
        int userid = req.attribute("userId");

        String nextPath = req.body().isEmpty() ? ""
                : JsonParser.parseString(req.body()).getAsJsonObject().get("path").getAsString();

        Connection sql = Database.connect();

        try {
            try (var stmt = sql.prepareStatement("SELECT uuid FROM users WHERE id = ?")) {
                stmt.setInt(1, userid);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String uuid = rs.getString("uuid");
                        String path = "storage/" + uuid + (nextPath.isEmpty() ? "" : "/" + nextPath);

                        Path filePath = Paths.get(path);
                        if (!Files.exists(filePath)) {
                            res.status(404);
                            JsonObject response = new JsonObject();
                            response.addProperty("error", "Arquivo não encontrado");
                            return response.toString();
                        }

                        RegisterLog Log = new RegisterLog(sql);

                        Log.create("Pego o conteúdo do arquivo", "o conteúdo do arquivo "
                                        + filePath
                                        + " foi obtido com sucesso",
                                userid);

                        String fileContent = getFileContent(filePath);
                        JsonObject response = new JsonObject();
                        response.addProperty("content", fileContent);

                        res.status(200);
                        return response.toString();
                    } else {
                        JsonObject response = new JsonObject();
                        response.addProperty("error", "Usuário não encontrado");
                        res.status(401);
                        return response.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject response = new JsonObject();
            res.status(500);
            response.addProperty("error", "Erro interno do servidor");
            return response.toString();
        } finally {
            sql.close();
        }
    }

    private static String getFileContent(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();

        // Verifica se é um arquivo de texto ou código
        if (fileName.endsWith(".txt") || fileName.endsWith(".odt") || fileName.endsWith(".docx") ||
                fileName.endsWith(".pdf") || fileName.endsWith(".java") || fileName.endsWith(".js") ||
                fileName.endsWith(".py") || fileName.endsWith(".ts") || fileName.endsWith(".json")) {
            return Files.readString(filePath);
        }

        // Verifica se é uma imagem
        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".gif") || fileName.endsWith(".bmp")) {
            byte[] fileBytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(fileBytes);
        }

        throw new IOException("Tipo de arquivo não suportado");
    }
}