package com.library.dao.impl;

import com.library.dao.AuthorDAO;
import com.library.exception.DatabaseException;
import com.library.model.Author;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAOImpl implements AuthorDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuthorDAOImpl.class);

    @Override
    public Author findById(Long id) {
        String sql = "SELECT id, name, biography FROM authors WHERE id = ?";
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
            logger.error("Error finding author by ID: {}", id, e);
            throw new DatabaseException("Failed to find author", e);
        }
    }

    @Override
    public Author findByName(String name) {
        String sql = "SELECT id, name, biography FROM authors WHERE LOWER(name) = LOWER(?)";
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
            logger.error("Error finding author by name: {}", name, e);
            throw new DatabaseException("Failed to find author by name", e);
        }
    }

    @Override
    public List<Author> findAll() {
        String sql = "SELECT id, name, biography FROM authors ORDER BY name ASC";
        List<Author> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error listing authors", e);
            throw new DatabaseException("Failed to list authors", e);
        }
    }

    @Override
    public Long findOrCreate(Connection conn, String name) {
        String trimmed = name.trim();
        String selectSql = "SELECT id FROM authors WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error looking up author: {}", trimmed, e);
            throw new DatabaseException("Failed to lookup author", e);
        }

        String insertSql = "INSERT INTO authors (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new DatabaseException("Failed to get generated key for new author");
        } catch (SQLException e) {
            logger.error("Error inserting author: {}", trimmed, e);
            throw new DatabaseException("Failed to create author", e);
        }
    }

    @Override
    public List<Author> findAuthorsByBookId(Long bookId) {
        String sql = "SELECT a.id, a.name, a.biography " +
                "FROM authors a " +
                "JOIN book_authors ba ON a.id = ba.author_id " +
                "WHERE ba.book_id = ? " +
                "ORDER BY a.name ASC";
        List<Author> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding authors for book: {}", bookId, e);
            throw new DatabaseException("Failed to find authors for book", e);
        }
    }

    @Override
    public void assignAuthorToBook(Connection conn, Long bookId, Long authorId) {
        String sql = "INSERT IGNORE INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ps.setLong(2, authorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error linking book {} to author {}", bookId, authorId, e);
            throw new DatabaseException("Failed to link author to book", e);
        }
    }

    @Override
    public void clearAuthorsForBook(Connection conn, Long bookId) {
        String sql = "DELETE FROM book_authors WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error clearing authors for book: {}", bookId, e);
            throw new DatabaseException("Failed to clear authors for book", e);
        }
    }

    private Author mapRow(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setId(rs.getLong("id"));
        author.setName(rs.getString("name"));
        author.setBiography(rs.getString("biography"));
        return author;
    }
}
