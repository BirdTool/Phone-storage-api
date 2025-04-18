package com.company.api.controllers.storageManager.controllers;

import com.company.api.database.Database;
import com.company.api.utils.RegisterFile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class postFile {
    public static String execute(spark.Request req, spark.Response res) throws SQLException {
        int userid = req.attribute("userId");

        Connection sql = Database.connect();

        String nextPath = req.queryParams("path") != null ? req.queryParams("path") : "";

        try {
            try (var stmt = sql.prepareStatement("SELECT uuid FROM users WHERE id = ?")) {
                stmt.setInt(1, userid);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String uuid = rs.getString("uuid");
                        String basePath = Paths.get("").toAbsolutePath().toString(); // Diretório atual do projeto
                        String relativePath = "storage/" + uuid + (nextPath.isEmpty() ? "" : "/" + nextPath);
                        String fullPath = Paths.get(basePath, relativePath).toString();


                        // Cria o diretório, se não existir
                        Files.createDirectories(Paths.get(fullPath));

                        // Habilita multipart para esta requisição
                        req.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));

                        // Obtém o arquivo enviado
                        String fileName = req.raw().getPart("file").getSubmittedFileName();
                        InputStream fileContent = req.raw().getPart("file").getInputStream();

                        // Salva o arquivo no caminho especificado
                        File targetFile = new File(fullPath, fileName);
                        try (FileOutputStream outStream = new FileOutputStream(targetFile)) {
                            fileContent.transferTo(outStream);
                        }

                        RegisterFile file = new RegisterFile(sql);
                        file.uploadFile(userid, fileName, fullPath);

                        JsonObject response = new JsonObject();
                        response.addProperty("message", "Arquivo enviado com sucesso");
                        res.status(200);
                        return response.toString();
                    } else {
                        JsonObject response = new JsonObject();
                        res.status(401);
                        response.addProperty("error", "Usuário não encontrado");
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
}