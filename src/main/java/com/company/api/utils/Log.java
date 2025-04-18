package com.company.api.utils;

import com.company.api.database.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class Log {
    public static void create(String title, String description, int userid, Connection existingConn) throws SQLException {
        // Usa a conexão existente em vez de criar uma nova
        try (var smtm = existingConn.prepareStatement("INSERT INTO logs (title, description, user_id) VALUES (?, ?, ?)")) {
            smtm.setString(1, title);
            smtm.setString(2, description);
            smtm.setInt(3, userid);
            smtm.executeUpdate();
        }
    }
    
    public static void create(String title, int userid, Connection existingConn) throws SQLException {
        try (var smtm = existingConn.prepareStatement("INSERT INTO logs (title, user_id) VALUES (?, ?)")) {
            smtm.setString(1, title);
            smtm.setInt(2, userid);
            smtm.executeUpdate();
        }
    }

    public static void create(String title, String description, int userid) throws SQLException {
        Connection sql = Database.connect();
        try (var smtm = sql.prepareStatement("INSERT INTO logs (title, description, user_id) VALUES (?, ?, ?)")) {
            smtm.setString(1, title);
            smtm.setString(2, description);
            smtm.setInt(3, userid);
            smtm.executeUpdate();
        } finally {
            sql.close();
        }
    }

    public static void create(String title, int userid) throws SQLException {
        Connection sql = Database.connect();
        try (var smtm = sql.prepareStatement("INSERT INTO logs (title, user_id) VALUES (?, ?)")) {
            smtm.setString(1, title);
            smtm.setInt(2, userid);
            smtm.executeUpdate();
        } finally {
            sql.close();
        }
    }
}
