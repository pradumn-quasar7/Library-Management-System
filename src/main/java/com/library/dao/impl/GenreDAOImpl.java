package com.library.dao.impl;

import com.library.dao.GenreDAO;
import com.library.exception.DatabaseException;
import com.library.model.Genre;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenreDAOImpl implements GenreDAO {
    private static final Logger logger = LoggerFactory.getLogger(GenreDAOImpl.class);

    @Override
    public Genre findById(Long id) {
        String sql = "SELECT id, name, description FROM genres WHERE id = ?";
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
            logger.error("Error finding genre by ID: {}", id, e);
            throw new DatabaseException("Failed to find genre", e);
        }
    }

    @Override
    public Genre findByName(String name) {
        String sql = "SELECT id, name, description FROM genres WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding genre by name: {}", name, e);
            throw new DatabaseException("Failed to find genre by name", e);
        }
    }

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT id, name, description FROM genres ORDER BY name ASC";
        List<Genre> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error listing genres", e);
            throw new DatabaseException("Failed to list genres", e);
        }
    }

    @Override
    public Long findOrCreate(Connection conn, String name) {
        String trimmed = name.trim();
        String selectSql = "SELECT id FROM genres WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking genre: {}", trimmed, e);
            throw new DatabaseException("Failed to check genre", e);
        }

        String insertSql = "INSERT INTO genres (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new DatabaseException("Failed to get generated key for new genre");
        } catch (SQLException e) {
            logger.error("Error inserting genre: {}", trimmed, e);
            throw new DatabaseException("Failed to create genre", e);
        }
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        genre.setDescription(rs.getString("description"));
        return genre;
    }
}
