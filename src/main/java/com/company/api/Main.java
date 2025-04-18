package com.company.api;

import com.company.api.controllers.auth.LoginRoute;
import com.company.api.controllers.storageManager.StorageManagerRoutes;
import com.company.api.database.Database;
import com.company.api.controllers.auth.AuthFilter;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(8080);
        Database.initialize();
        get("/", (req, res) -> "Hello Java!");
        LoginRoute.setupRoutes();
        StorageManagerRoutes.setupRoutes();

        AuthFilter.setupFilters();

        logs();
    }

    private static void logs() {
        before((req, res) -> {
            System.out.println(req.requestMethod() + " " + req.pathInfo());
        });
        after((req, res) -> {
            System.out.println(res.status() + " " + req.requestMethod() + " " + req.pathInfo());
        });
    }
}

