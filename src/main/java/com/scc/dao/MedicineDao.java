package com.scc.dao;

import com.scc.model.Medicine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicineDao {

    /** Adds a new medicine. Returns its generated ID, or -1 if it failed (e.g. duplicate name). */
    public int addMedicine(String name, double price, int stock) {
        String sql = "INSERT INTO medicines(name, price, stock) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return -1;
        }
    }

    /** Adds 'quantity' units to a medicine's stock. Returns true if the medicine exists and was updated. */
    public boolean addStock(int medicineId, int quantity) {
        String sql = "UPDATE medicines SET stock = stock + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, medicineId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    public Medicine getMedicineById(int medicineId) {
        String sql = "SELECT id, name, price, stock FROM medicines WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, medicineId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Medicine(rs.getInt("id"), rs.getString("name"),
                            rs.getDouble("price"), rs.getInt("stock"));
                }
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
        }
        return null;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT id, name, price, stock FROM medicines ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicines.add(new Medicine(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getInt("stock")));
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
        }
        return medicines;
    }
}