package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Action type constants, so the same spelling is used everywhere in the app.
    public static final String USER_APPROVED = "USER_APPROVED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String LOGS_CLEARED = "LOGS_CLEARED";
    // Ready for the pharmacy features (Pharmacist / Cashier menus) once they exist.
    public static final String MEDICINE_ADDED = "MEDICINE_ADDED";
    public static final String STOCK_UPDATED = "STOCK_UPDATED";
    public static final String SALE_PROCESSED = "SALE_PROCESSED";

    /** Inserts one audit entry. Never throws: a logging failure must not break the action being logged. */
    public boolean logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs(user_id, action_type, description) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LOGGING FAILED: " + e.getMessage());
            return false;
        }
    }

    /** Returns every log entry, newest first. */
    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT id, user_id, action_type, description, timestamp "
                + "FROM activity_logs ORDER BY timestamp DESC, id DESC";
        return queryLogs(sql, null);
    }

    /** Returns the log entries of one user, newest first. */
    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT id, user_id, action_type, description, timestamp "
                + "FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        return queryLogs(sql, userId);
    }

    /** Deletes all log entries. Returns how many rows were removed, or -1 on failure. */
    public int clearAllLogs() {
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return -1;
        }
    }

    private List<ActivityLog> queryLogs(String sql, Integer userId) {
        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                pstmt.setInt(1, userId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getString("action_type"), rs.getString("description"),
                            rs.getString("timestamp")));
                }
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
        }
        return logs;
    }
}