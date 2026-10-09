package com.library.dao.impl;

import com.library.dao.BookCopyDAO;
import com.library.exception.DatabaseException;
import com.library.model.BookCopy;
import com.library.model.CopyStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookCopyDAOImpl implements BookCopyDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookCopyDAOImpl.class);

    private static final String BASE_QUERY =
            "SELECT c.id, c.book_id, c.accession_number, c.status, c.condition_notes, c.created_at, " +
            "b.title AS book_title, b.isbn AS book_isbn " +
            "FROM book_copies c " +
            "JOIN books b ON c.book_id = b.id ";

    @Override
    public BookCopy findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Error finding copy by id: {}", id, e);
            throw new DatabaseException("Failed to find copy", e);
        }
    }

    @Override
    public BookCopy findById(Connection conn, Long id) {
        String sql = BASE_QUERY + "WHERE c.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding copy by id in conn: {}", id, e);
            throw new DatabaseException("Failed to find copy record", e);
        }
    }

    @Override
    public BookCopy findByAccessionNumber(String accessionNumber) {
        String sql = BASE_QUERY + "WHERE LOWER(c.accession_number) = LOWER(?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accessionNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding copy by accession: {}", accessionNumber, e);
            throw new DatabaseException("Failed to find copy by accession number", e);
        }
    }

    @Override
    public List<BookCopy> findCopiesByBookId(Long bookId) {
        String sql = BASE_QUERY + "WHERE c.book_id = ? ORDER BY c.accession_number ASC";
        List<BookCopy> list = new ArrayList<>();
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
            logger.error("Error listing copies for book: {}", bookId, e);
            throw new DatabaseException("Failed to list copies", e);
        }
    }

    @Override
    public BookCopy findFirstAvailableCopy(Connection conn, Long bookId) {
        // FOR UPDATE locks the selected row to prevent concurrent race conditions
        String sql = BASE_QUERY + "WHERE c.book_id = ? AND c.status = 'AVAILABLE' ORDER BY c.id ASC LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error selecting available copy for book {} with lock", bookId, e);
            throw new DatabaseException("Failed to lock available book copy", e);
        }
    }

    @Override
    public Long create(Connection conn, BookCopy copy) {
        String sql = "INSERT INTO book_copies (book_id, accession_number, status, condition_notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, copy.getBookId());
            ps.setString(2, copy.getAccessionNumber().trim());
            ps.setString(3, copy.getStatus() != null ? copy.getStatus().name() : CopyStatus.AVAILABLE.name());
            ps.setString(4, copy.getConditionNotes() != null ? copy.getConditionNotes() : "Good condition");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        copy.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to create copy, no ID returned.");
        } catch (SQLException e) {
            logger.error("Error creating book copy", e);
            throw new DatabaseException("Failed to insert book copy", e);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Long copyId, CopyStatus status) {
        String sql = "UPDATE book_copies SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, copyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for copy {}", copyId, e);
            throw new DatabaseException("Failed to update copy status", e);
        }
    }

    @Override
    public boolean updateCondition(Long copyId, String conditionNotes) {
        String sql = "UPDATE book_copies SET condition_notes = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conditionNotes);
            ps.setLong(2, copyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating condition notes for copy {}", copyId, e);
            throw new DatabaseException("Failed to update copy condition", e);
        }
    }

    @Override
    public int countCopiesByStatus(CopyStatus status) {
        String sql = "SELECT COUNT(*) FROM book_copies WHERE status = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting copies by status {}", status, e);
            throw new DatabaseException("Failed to count copies by status", e);
        }
    }

    @Override
    public int countTotalCopies() {
        String sql = "SELECT COUNT(*) FROM book_copies";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting total copies", e);
            throw new DatabaseException("Failed to count total copies", e);
        }
    }

    private BookCopy mapRow(ResultSet rs) throws SQLException {
        BookCopy copy = new BookCopy();
        copy.setId(rs.getLong("id"));
        copy.setBookId(rs.getLong("book_id"));
        copy.setAccessionNumber(rs.getString("accession_number"));
        copy.setStatus(CopyStatus.fromString(rs.getString("status")));
        copy.setConditionNotes(rs.getString("condition_notes"));
        copy.setCreatedAt(rs.getTimestamp("created_at"));
        copy.setBookTitle(rs.getString("book_title"));
        copy.setBookIsbn(rs.getString("book_isbn"));
        return copy;
    }
}
