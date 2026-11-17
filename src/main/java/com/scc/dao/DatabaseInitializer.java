package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String users = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL CHECK (roles IN ('ADMIN','PHARMACIST','CASHIER')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )""";

        String medicines = """
                CREATE TABLE IF NOT EXISTS medicines (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    price REAL NOT NULL CHECK (price >= 0),
                    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0)
                )""";

        String sales = """
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cashier_id INTEGER NOT NULL,
                    sale_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    total REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY (cashier_id) REFERENCES users(id)
                )""";

        String saleItems = """
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL,
                    medicine_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL CHECK (quantity > 0),
                    unit_price REAL NOT NULL,
                    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
                    FOREIGN KEY (medicine_id) REFERENCES medicines(id)
                )""";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            // Parent tables first, so foreign keys always have something to point to.
            stmt.execute(users);
            stmt.execute(medicines);
            stmt.execute(sales);
            stmt.execute(saleItems);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }
}