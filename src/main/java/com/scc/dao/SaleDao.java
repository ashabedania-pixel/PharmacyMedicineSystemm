package com.scc.dao;

import com.scc.model.SaleReceipt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SaleDao {

    /**
     * Sells 'quantity' units of one medicine as a single all-or-nothing transaction:
     * stock is reduced, a sales row and a sale_items row are written, or nothing changes.
     * Returns a receipt, or null if the medicine is missing, stock is too low, or a database error occurred.
     */
    public SaleReceipt processSale(int cashierId, int medicineId, int quantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String name;
                double price;
                try (PreparedStatement ps = conn.prepareStatement("SELECT name, price FROM medicines WHERE id = ?")) {
                    ps.setInt(1, medicineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return null;
                        }
                        name = rs.getString("name");
                        price = rs.getDouble("price");
                    }
                }

                // The "stock >= ?" condition stops stock from ever going negative.
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE medicines SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, medicineId);
                    ps.setInt(3, quantity);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return null;
                    }
                }

                double total = price * quantity;
                int saleId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sales(cashier_id, total) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, cashierId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        saleId = keys.next() ? keys.getInt(1) : -1;
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sale_items(sale_id, medicine_id, quantity, unit_price) VALUES(?, ?, ?, ?)")) {
                    ps.setInt(1, saleId);
                    ps.setInt(2, medicineId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, price);
                    ps.executeUpdate();
                }

                conn.commit();
                return new SaleReceipt(saleId, name, quantity, price, total);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return null;
        }
    }
}