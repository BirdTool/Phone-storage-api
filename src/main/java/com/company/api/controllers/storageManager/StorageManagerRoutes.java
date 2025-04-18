package com.company.api.controllers.storageManager;

import static spark.Spark.*;

import com.company.api.controllers.storageManager.controllers.getUserFiles;
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
    }
}