package com.company.api.controllers.auth;

import static spark.Spark.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.company.api.utils.RegisterLog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.company.api.database.Database;
import com.company.api.utils.JwtTokenUtil;

public class LoginRoute {
    public static void setupRoutes() {
        post("/login", (req, res) -> {
            res.type("application/json");
            return login(req, res);
        });
    }

    private static String login(spark.Request req, spark.Response res) {
        try {
            JsonObject jsonObject = new Gson().fromJson(req.body(), JsonObject.class);
            String username = jsonObject.get("username").getAsString();
            String password = jsonObject.get("password").getAsString();
            
            Map<String, Object> authResult = validateLogin(username, password);
            boolean loginSuccess = (boolean) authResult.get("success");
            int userId = (int) authResult.get("userId");
            
            if (loginSuccess) {
                // Gera o token JWT
                String token = JwtTokenUtil.generateToken(userId);
                
                // Configura o cookie
                res.cookie("/", "token", token, 86400, false, true); // 24 horas, HTTP Only
                
                // Retorna a resposta
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("message", "Login bem-sucedido");
                response.addProperty("userId", userId);
                return response.toString();
            } else {
                res.status(401);
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Credenciais inválidas");
                return response.toString();
            }
        } catch (Exception e) {
            res.status(400);
            JsonObject response = new JsonObject();
            response.addProperty("success", false);
            response.addProperty("message", "Erro no servidor");
            return response.toString();
        }
    }
    
    private static Map<String, Object> validateLogin(String username, String password) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        Connection sql = Database.connect();
        
        try {
            try (var stmt = sql.prepareStatement("SELECT password, id FROM users WHERE username = ?")) {
                stmt.setString(1, username);
                
                try (var rs = stmt.executeQuery()) {
                    RegisterLog log = new RegisterLog(sql);
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        int userId = rs.getInt("id");
    
                        if (storedPassword.equals(password)) {
                            log.create("Sucesso ao fazer login", userId);
                            result.put("success", true);
                            result.put("userId", userId);
                        } else {
                            log.create("Login mal sucedido", "Senha incorreta", userId);
                        }
                    }
                }
            }
        } finally {
            sql.close();
        }
        
        return result;
    }
}