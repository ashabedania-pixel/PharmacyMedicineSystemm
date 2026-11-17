package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("SUCCESSFUL REGISTRATION. Waiting for approval.");
            return true;
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    /** Returns the matching user (any status), or null if credentials are wrong. */
    public User login(String username, String password) {
        String sql = "SELECT id, username, roles, status FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"),
                            rs.getString("roles"), rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
        }
        return null;
    }

    /** Returns every registered account, regardless of role or status. */
    public List<User> getAllUsers() {
        return queryUsers("SELECT id, username, roles, status FROM users ORDER BY id");
    }

    /** Returns all accounts with status 'PENDING', across every role. */
    public List<User> getPendingUsers() {
        return queryUsers("SELECT id, username, roles, status FROM users WHERE status = 'PENDING' ORDER BY id");
    }

    /** Sets the given user's status to 'APPROVED'. Returns true if a row was updated. */
    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a user's role and status in one statement.
     * Both values are upper-cased before they reach the database.
     * Returns true if a row was updated.
     */
    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    private List<User> queryUsers(String sql) {
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("roles"), rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("FAILED: " + e.getMessage());
        }
        return users;
    }
}