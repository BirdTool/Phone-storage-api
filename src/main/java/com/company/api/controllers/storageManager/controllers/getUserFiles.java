package com.company.api.controllers.storageManager.controllers;

import com.company.api.database.Database;
import com.company.api.utils.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class getUserFiles {
    private static final Logger log = LoggerFactory.getLogger(getUserFiles.class);

    public static String execute(spark.Request req, spark.Response res) throws SQLException {
        int userid = req.attribute("userId");

        Connection sql = Database.connect();

        try {
            try (var stmt = sql.prepareStatement("SELECT uuid FROM USERS WHERE id = ?")) {
                stmt.setInt(1, userid);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String uuid = rs.getString("uuid");
                        String path = "storage/" + uuid;

                        JsonObject filesJson = buildFileStructure(Paths.get(path));

                        res.status(200);
                        JsonObject response = new JsonObject();
                        response.addProperty("userid", userid);
                        response.add("files", filesJson);

                        Log.create("Acesso aos arquivos", "os arquivos foram acessados com sucesso", userid, sql);

                        return response.toString();
                    } else {
                        res.status(404);
                        JsonObject response = new JsonObject();
                        response.addProperty("error", "Usuário não encontrado");
                        return response.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erro ao buscar arquivos do usuário", e);
            res.status(500);
            JsonObject response = new JsonObject();
            response.addProperty("error", "Erro interno do servidor");
            return response.toString();
        } finally {
            sql.close();
        }
    }

    private static JsonObject buildFileStructure(Path rootPath) {
        JsonObject filesObject = new JsonObject();
        
        try {
            if (Files.exists(rootPath)) {
                processDirectory(rootPath, filesObject, rootPath);
            }
        } catch (Exception e) {
            log.error("Erro ao processar estrutura de arquivos", e);
        }
        
        return filesObject;
    }
    
    private static void processDirectory(Path dirPath, JsonObject parentJson, Path rootPath) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path path : stream) {
                String relativePath = rootPath.relativize(path).toString();
                if (relativePath.isEmpty()) continue;
                
                String fileName = path.getFileName().toString();
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d");
                String createdAt = dateFormat.format(new Date(attrs.creationTime().toMillis()));
                
                JsonObject fileObject = new JsonObject();
                
                if (Files.isDirectory(path)) {
                    fileObject.addProperty("type", "directory");
                    fileObject.addProperty("createdAt", createdAt);
                    
                    JsonArray children = new JsonArray();
                    fileObject.add("children", children);
                    
                    // Process all files and subdirectories in this directory
                    processChildren(path, children);
                } else {
                    fileObject.addProperty("type", "file");
                    fileObject.addProperty("createdAt", createdAt);
                }
                
                parentJson.add(fileName, fileObject);
            }
        }
    }
    
    private static void processChildren(Path dirPath, JsonArray childrenArray) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path childPath : stream) {
                JsonObject childObject = new JsonObject();
                String childName = childPath.getFileName().toString();
                BasicFileAttributes childAttrs = Files.readAttributes(childPath, BasicFileAttributes.class);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d");
                String childCreatedAt = dateFormat.format(new Date(childAttrs.creationTime().toMillis()));
                
                childObject.addProperty("name", childName);
                
                if (Files.isDirectory(childPath)) {
                    childObject.addProperty("type", "directory");
                    childObject.addProperty("createdAt", childCreatedAt);
                    
                    JsonArray nestedChildren = new JsonArray();
                    childObject.add("children", nestedChildren);
                    
                    // Recursively process subdirectories
                    processChildren(childPath, nestedChildren);
                } else {
                    childObject.addProperty("type", "file");
                    childObject.addProperty("createdAt", childCreatedAt);
                }
                
                childrenArray.add(childObject);
            }
        }
    }
}
