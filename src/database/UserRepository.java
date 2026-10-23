package database;

import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    public User findById(int userId) throws SQLException {
        String sql = "SELECT id, full_name FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? new User(rs.getInt("id"), rs.getString("full_name")) : null;
            }
        }
    }

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, full_name, password FROM users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                int id = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String storedPassword = rs.getString("password");
                boolean matches = PasswordHasher.isHashed(storedPassword)
                        ? PasswordHasher.verify(password, storedPassword)
                        : password.equals(storedPassword);
                if (!matches) return null;
                if (!PasswordHasher.isHashed(storedPassword)) {
                    try (PreparedStatement upgrade = conn.prepareStatement(
                            "UPDATE users SET password=? WHERE id=?")) {
                        upgrade.setString(1, PasswordHasher.hash(password));
                        upgrade.setInt(2, id);
                        upgrade.executeUpdate();
                    }
                }
                return new User(id, fullName);
            }
        }
    }

    /** Returns false when the username already exists. */
    public boolean register(String fullName, String username, String password) throws SQLException {
        String sql = "INSERT INTO users(full_name, username, password) VALUES(?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, PasswordHasher.hash(password));
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) return false;
            throw e;
        }
    }
}
