package com.library.dao.impl;

import com.library.dao.UserDAO;
import com.library.exception.DatabaseException;
import com.library.model.Role;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public User findById(Long id) {
        String sql = "SELECT id, email, password_hash, role, status, created_at, updated_at FROM users WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new DatabaseException("Failed to find user by ID", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, email, password_hash, role, status, created_at, updated_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DatabaseException("Failed to find user by email", e);
        }
    }

    @Override
    public Long create(User user) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return create(conn, user);
        } catch (SQLException e) {
            logger.error("Error creating user: {}", user.getEmail(), e);
            throw new DatabaseException("Failed to create user", e);
        }
    }

    @Override
    public Long create(Connection conn, User user) {
        String sql = "INSERT INTO users (email, password_hash, role, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail().trim().toLowerCase());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getStatus() != null ? user.getStatus().name() : UserStatus.ACTIVE.name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        user.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Creating user failed, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error inserting user in transaction: {}", user.getEmail(), e);
            throw new DatabaseException("Failed to insert user record", e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET email = ?, role = ?, status = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getEmail().trim().toLowerCase());
            ps.setString(2, user.getRole().name());
            ps.setString(3, user.getStatus().name());
            ps.setLong(4, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getId(), e);
            throw new DatabaseException("Failed to update user", e);
        }
    }

    @Override
    public boolean updatePassword(Long id, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for user: {}", id, e);
            throw new DatabaseException("Failed to update password", e);
        }
    }

    @Override
    public boolean updateStatus(Long id, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user status: {}", id, e);
            throw new DatabaseException("Failed to update user status", e);
        }
    }

    @Override
    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting active users", e);
            throw new DatabaseException("Failed to count users", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, email, password_hash, role, status, created_at, updated_at FROM users ORDER BY id DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error listing users", e);
            throw new DatabaseException("Failed to retrieve users", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setStatus(UserStatus.fromString(rs.getString("status")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
