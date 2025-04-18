package com.company.api.controllers.storageManager;

import static spark.Spark.*;

import com.company.api.controllers.storageManager.controllers.getContentFile;
import com.company.api.controllers.storageManager.controllers.getFile;
import com.company.api.controllers.storageManager.controllers.getUserFiles;
import com.company.api.controllers.storageManager.controllers.postFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageManagerRoutes {
    public static void setupRoutes() {
        get("/auth/files", (req, res) -> {
            try {
                return getUserFiles.execute(req, res);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Erro interno\"}";
            }
        });
        get("/auth/content/file", (req, res) -> {
            try {
                return getContentFile.execute(req, res);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Erro interno\"}";
            }
        });
        post("auth/file", (req, res) -> {
            try {
                return postFile.execute(req, res);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Erro interno\"}";
            }
        });
        get("auth/download/file", (req, res) -> {
            try {
                return getFile.execute(req, res);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"Erro interno\"}";
            }
        });
    }
}