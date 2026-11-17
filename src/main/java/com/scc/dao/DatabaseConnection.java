package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:Pharmacy.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        // SQLite disables foreign keys by default and the setting is per-connection,
        // so it must be switched on every time a connection is created.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}