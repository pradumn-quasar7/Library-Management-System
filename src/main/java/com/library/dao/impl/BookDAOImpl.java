package com.library.dao.impl;

import com.library.dao.AuthorDAO;
import com.library.dao.BookDAO;
import com.library.exception.DatabaseException;
import com.library.model.Book;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAOImpl implements BookDAO {
    private static final Logger logger = LoggerFactory.getLogger(BookDAOImpl.class);
    private final AuthorDAO authorDAO = new AuthorDAOImpl();

    private static final String BASE_QUERY =
            "SELECT b.id, b.title, b.isbn, b.publisher, b.publication_year, b.description, b.genre_id, b.status, " +
            "b.created_at, b.updated_at, g.name AS genre_name, " +
            "COUNT(c.id) AS total_copies, " +
            "SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies " +
            "FROM books b " +
            "LEFT JOIN genres g ON b.genre_id = g.id " +
            "LEFT JOIN book_copies c ON b.id = c.book_id ";

    private static final String GROUP_BY =
            " GROUP BY b.id, b.title, b.isbn, b.publisher, b.publication_year, b.description, b.genre_id, b.status, " +
            "b.created_at, b.updated_at, g.name ";

    @Override
    public Book findById(Long id) {
        String sql = BASE_QUERY + "WHERE b.id = ?" + GROUP_BY;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Book book = mapRow(rs);
                    book.setAuthors(authorDAO.findAuthorsByBookId(book.getId()));
                    return book;
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding book by id: {}", id, e);
            throw new DatabaseException("Failed to find book by ID", e);
        }
    }

    @Override
    public Book findByIsbn(String isbn) {
        String sql = BASE_QUERY + "WHERE LOWER(b.isbn) = LOWER(?)" + GROUP_BY;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Book book = mapRow(rs);
                    book.setAuthors(authorDAO.findAuthorsByBookId(book.getId()));
                    return book;
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding book by ISBN: {}", isbn, e);
            throw new DatabaseException("Failed to find book by ISBN", e);
        }
    }

    @Override
    public List<Book> findAll(boolean activeOnly) {
        StringBuilder sb = new StringBuilder(BASE_QUERY);
        if (activeOnly) {
            sb.append("WHERE b.status = 'ACTIVE' ");
        }
        sb.append(GROUP_BY);
        sb.append("ORDER BY b.title ASC");

        List<Book> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sb.toString())) {
            while (rs.next()) {
                Book book = mapRow(rs);
                book.setAuthors(authorDAO.findAuthorsByBookId(book.getId()));
                list.add(book);
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error listing all books", e);
            throw new DatabaseException("Failed to list books", e);
        }
    }

    @Override
    public List<Book> search(String query, Long genreId, Boolean availableOnly, String sortBy) {
        StringBuilder sb = new StringBuilder(
                "SELECT DISTINCT b.id, b.title, b.isbn, b.publisher, b.publication_year, b.description, b.genre_id, b.status, " +
                "b.created_at, b.updated_at, g.name AS genre_name, " +
                "COUNT(DISTINCT c.id) AS total_copies, " +
                "SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies " +
                "FROM books b " +
                "LEFT JOIN genres g ON b.genre_id = g.id " +
                "LEFT JOIN book_copies c ON b.id = c.book_id " +
                "LEFT JOIN book_authors ba ON b.id = ba.book_id " +
                "LEFT JOIN authors a ON ba.author_id = a.id " +
                "WHERE b.status = 'ACTIVE' "
        );

        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            sb.append("AND (LOWER(b.title) LIKE ? OR LOWER(b.isbn) LIKE ? OR LOWER(a.name) LIKE ? OR LOWER(b.publisher) LIKE ?) ");
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        if (genreId != null && genreId > 0) {
            sb.append("AND b.genre_id = ? ");
            params.add(genreId);
        }

        sb.append(GROUP_BY);

        if (Boolean.TRUE.equals(availableOnly)) {
            sb.append("HAVING available_copies > 0 ");
        }

        if ("year_desc".equalsIgnoreCase(sortBy)) {
            sb.append("ORDER BY b.publication_year DESC, b.title ASC");
        } else if ("year_asc".equalsIgnoreCase(sortBy)) {
            sb.append("ORDER BY b.publication_year ASC, b.title ASC");
        } else if ("title_desc".equalsIgnoreCase(sortBy)) {
            sb.append("ORDER BY b.title DESC");
        } else {
            sb.append("ORDER BY b.title ASC");
        }

        List<Book> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = mapRow(rs);
                    book.setAuthors(authorDAO.findAuthorsByBookId(book.getId()));
                    list.add(book);
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error searching books", e);
            throw new DatabaseException("Failed to search books", e);
        }
    }

    @Override
    public Long create(Connection conn, Book book) {
        String sql = "INSERT INTO books (title, isbn, publisher, publication_year, description, genre_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle().trim());
            ps.setString(2, book.getIsbn().trim());
            ps.setString(3, book.getPublisher());
            if (book.getPublicationYear() != null) {
                ps.setInt(4, book.getPublicationYear());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, book.getDescription());
            if (book.getGenreId() != null) {
                ps.setLong(6, book.getGenreId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setString(7, book.getStatus() != null ? book.getStatus() : "ACTIVE");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        book.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to insert book, no ID generated.");
        } catch (SQLException e) {
            logger.error("Error inserting book in transaction: {}", book.getTitle(), e);
            throw new DatabaseException("Failed to create book", e);
        }
    }

    @Override
    public boolean update(Connection conn, Book book) {
        String sql = "UPDATE books SET title = ?, isbn = ?, publisher = ?, publication_year = ?, description = ?, genre_id = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle().trim());
            ps.setString(2, book.getIsbn().trim());
            ps.setString(3, book.getPublisher());
            if (book.getPublicationYear() != null) {
                ps.setInt(4, book.getPublicationYear());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, book.getDescription());
            if (book.getGenreId() != null) {
                ps.setLong(6, book.getGenreId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setString(7, book.getStatus());
            ps.setLong(8, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating book: {}", book.getId(), e);
            throw new DatabaseException("Failed to update book", e);
        }
    }

    @Override
    public boolean setStatus(Long id, String status) {
        String sql = "UPDATE books SET status = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating book status: {}", id, e);
            throw new DatabaseException("Failed to update book status", e);
        }
    }

    @Override
    public int countTotalBooks() {
        String sql = "SELECT COUNT(*) FROM books WHERE status = 'ACTIVE'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting total books", e);
            throw new DatabaseException("Failed to count books", e);
        }
    }

    @Override
    public int countAvailableBooks() {
        String sql = "SELECT COUNT(DISTINCT b.id) FROM books b JOIN book_copies c ON b.id = c.book_id WHERE b.status = 'ACTIVE' AND c.status = 'AVAILABLE'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting available books", e);
            throw new DatabaseException("Failed to count available books", e);
        }
    }

    @Override
    public List<Book> findTopPopularBooks(int limit) {
        String sql =
                "SELECT b.id, b.title, b.isbn, b.publisher, b.publication_year, b.description, b.genre_id, b.status, " +
                "b.created_at, b.updated_at, g.name AS genre_name, " +
                "COUNT(DISTINCT c.id) AS total_copies, " +
                "SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies, " +
                "COUNT(l.id) AS borrow_count " +
                "FROM books b " +
                "LEFT JOIN genres g ON b.genre_id = g.id " +
                "LEFT JOIN book_copies c ON b.id = c.book_id " +
                "LEFT JOIN loans l ON c.id = l.copy_id " +
                "WHERE b.status = 'ACTIVE' " +
                GROUP_BY +
                "ORDER BY borrow_count DESC, b.title ASC LIMIT ?";

        List<Book> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = mapRow(rs);
                    book.setAuthors(authorDAO.findAuthorsByBookId(book.getId()));
                    list.add(book);
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding popular books", e);
            throw new DatabaseException("Failed to find popular books", e);
        }
    }

    @Override
    public List<Book> findRecentlyAddedBooks(int limit) {
        String sql = BASE_QUERY + "WHERE b.status = 'ACTIVE' " + GROUP_BY + "ORDER BY b.created_at DESC LIMIT ?";
        List<Book> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = mapRow(rs);
                    book.setAuthors(authorDAO.findAuthorsByBookId(book.getId()));
                    list.add(book);
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding recently added books", e);
            throw new DatabaseException("Failed to find recently added books", e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        int year = rs.getInt("publication_year");
        if (!rs.wasNull()) {
            book.setPublicationYear(year);
        }
        book.setDescription(rs.getString("description"));
        long genreId = rs.getLong("genre_id");
        if (!rs.wasNull()) {
            book.setGenreId(genreId);
        }
        book.setGenreName(rs.getString("genre_name"));
        book.setStatus(rs.getString("status"));
        book.setCreatedAt(rs.getTimestamp("created_at"));
        book.setUpdatedAt(rs.getTimestamp("updated_at"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        return book;
    }
}
