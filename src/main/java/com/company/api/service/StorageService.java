package com.company.api.service;

import java.io.File;
import java.util.UUID;

public class StorageService {

    // Caminho base ajustado para seu cenário
    private static final String STORAGE_ROOT = "./storage/";

    public File getUserStoragePath(UUID userId) {
        String userPath = STORAGE_ROOT + userId.toString() + "/";
        File dir = new File(userPath);
        if (!dir.exists()) {
            dir.mkdirs();  // Cria a pasta se não existir
        }
        return dir;
    }
}
