package com.company.api.controllers.auth;

import static spark.Spark.before;
import static spark.Spark.halt;

import com.company.api.utils.JwtTokenUtil;

public class AuthFilter {
    public static void setupFilters() {
        before((req, res) -> {
            if (req.pathInfo().startsWith("/auth")) {
                String token = req.cookie("token");

                if (token == null || !JwtTokenUtil.validateToken(token)) {
                    halt(401, "{\"success\": false, \"message\": \"Acesso não autorizado\"}");
                }

                req.attribute("userId", JwtTokenUtil.getUserIdFromToken(token));
            }
        });
    }
}
