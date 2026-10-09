package com.library.service;

import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.exception.ConflictException;
import com.library.exception.DatabaseException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.util.ConnectionManager;
import com.library.validation.BookValidator;
import com.library.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookDAO bookDAO;
    private final BookCopyDAO bookCopyDAO;
    private final AuthorDAO authorDAO;
    private final GenreDAO genreDAO;
    private final AuditLogDAO auditLogDAO;

    public BookService() {
        this.bookDAO = new BookDAOImpl();
        this.bookCopyDAO = new BookCopyDAOImpl();
        this.authorDAO = new AuthorDAOImpl();
        this.genreDAO = new GenreDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public BookService(BookDAO bookDAO, BookCopyDAO bookCopyDAO, AuthorDAO authorDAO, GenreDAO genreDAO, AuditLogDAO auditLogDAO) {
        this.bookDAO = bookDAO;
        this.bookCopyDAO = bookCopyDAO;
        this.authorDAO = authorDAO;
        this.genreDAO = genreDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Book getBookById(Long id) {
        Book book = bookDAO.findById(id);
        if (book == null) {
            throw new ResourceNotFoundException("Book not found with ID: " + id);
        }
        return book;
    }

    public Book getBookByIsbn(String isbn) {
        return bookDAO.findByIsbn(isbn);
    }

    public List<Book> getAllBooks(boolean activeOnly) {
        return bookDAO.findAll(activeOnly);
    }

    public List<Book> searchBooks(String query, Long genreId, Boolean availableOnly, String sortBy) {
        return bookDAO.search(query, genreId, availableOnly, sortBy);
    }

    public Book createBook(Book book, List<String> authorNames, int initialCopiesCount, Long actorUserId) {
        ValidationResult vr = BookValidator.validate(book);
        if (!vr.isValid()) {
            throw new ValidationException(vr.getErrors());
        }

        if (bookDAO.findByIsbn(book.getIsbn()) != null) {
            throw new ConflictException("A book with ISBN " + book.getIsbn() + " already exists.");
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            Long bookId = bookDAO.create(conn, book);
            book.setId(bookId);

            // Assign Authors
            if (authorNames != null) {
                for (String authorName : authorNames) {
                    if (authorName != null && !authorName.trim().isEmpty()) {
                        Long authorId = authorDAO.findOrCreate(conn, authorName.trim());
                        authorDAO.assignAuthorToBook(conn, bookId, authorId);
                    }
                }
            }

            // Create initial copies
            int copiesToCreate = Math.max(1, initialCopiesCount);
            String isbnSuffix = book.getIsbn().replaceAll("[-\\s]", "");
            if (isbnSuffix.length() > 6) {
                isbnSuffix = isbnSuffix.substring(isbnSuffix.length() - 6);
            }
            for (int i = 1; i <= copiesToCreate; i++) {
                BookCopy copy = new BookCopy();
                copy.setBookId(bookId);
                copy.setAccessionNumber("ACC-" + isbnSuffix + "-C" + i);
                copy.setStatus(CopyStatus.AVAILABLE);
                copy.setConditionNotes("Initial stock copy");
                bookCopyDAO.create(conn, copy);
            }

            auditLogDAO.create(conn, new AuditLog(actorUserId, "BOOK_CREATED", "BOOK", bookId, "Created book: " + book.getTitle() + " with " + copiesToCreate + " copies"));

            conn.commit();
            logger.info("Successfully created book {} (ID: {}) with {} copies", book.getTitle(), bookId, copiesToCreate);
            return getBookById(bookId);
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Transaction failed during book creation", e);
            throw new DatabaseException("Failed to create book", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public void updateBook(Book book, List<String> authorNames, Long actorUserId) {
        ValidationResult vr = BookValidator.validate(book);
        if (!vr.isValid()) {
            throw new ValidationException(vr.getErrors());
        }

        Book existing = bookDAO.findById(book.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("Book not found with ID: " + book.getId());
        }

        Book isbnCheck = bookDAO.findByIsbn(book.getIsbn());
        if (isbnCheck != null && !isbnCheck.getId().equals(book.getId())) {
            throw new ConflictException("Another book with ISBN " + book.getIsbn() + " already exists.");
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            bookDAO.update(conn, book);

            if (authorNames != null) {
                authorDAO.clearAuthorsForBook(conn, book.getId());
                for (String authorName : authorNames) {
                    if (authorName != null && !authorName.trim().isEmpty()) {
                        Long authorId = authorDAO.findOrCreate(conn, authorName.trim());
                        authorDAO.assignAuthorToBook(conn, book.getId(), authorId);
                    }
                }
            }

            auditLogDAO.create(conn, new AuditLog(actorUserId, "BOOK_UPDATED", "BOOK", book.getId(), "Updated metadata for book: " + book.getTitle()));

            conn.commit();
            logger.info("Updated book ID {}", book.getId());
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Transaction failed during book update", e);
            throw new DatabaseException("Failed to update book", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public void setBookStatus(Long bookId, String status, Long actorUserId) {
        Book book = getBookById(bookId);
        bookDAO.setStatus(bookId, status);
        auditLogDAO.create(new AuditLog(actorUserId, "BOOK_STATUS_CHANGED", "BOOK", bookId, "Changed status of '" + book.getTitle() + "' to " + status));
        logger.info("Changed status of book ID {} to {}", bookId, status);
    }

    public BookCopy addCopy(Long bookId, String accessionNumber, String conditionNotes, Long actorUserId) {
        Book book = getBookById(bookId);
        if (accessionNumber == null || accessionNumber.trim().isEmpty()) {
            accessionNumber = "ACC-" + bookId + "-C" + (System.currentTimeMillis() % 10000);
        }

        if (bookCopyDAO.findByAccessionNumber(accessionNumber) != null) {
            throw new ConflictException("Accession number " + accessionNumber + " is already in use.");
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            BookCopy copy = new BookCopy();
            copy.setBookId(bookId);
            copy.setAccessionNumber(accessionNumber.trim());
            copy.setStatus(CopyStatus.AVAILABLE);
            copy.setConditionNotes(conditionNotes != null ? conditionNotes.trim() : "Good condition");

            Long copyId = bookCopyDAO.create(conn, copy);
            copy.setId(copyId);

            auditLogDAO.create(conn, new AuditLog(actorUserId, "COPY_ADDED", "BOOK_COPY", copyId, "Added new copy " + accessionNumber + " for book: " + book.getTitle()));

            conn.commit();
            return copy;
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Error adding copy to book {}", bookId, e);
            throw new DatabaseException("Failed to add book copy", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public void updateCopyStatus(Long copyId, CopyStatus status, String conditionNotes, Long actorUserId) {
        BookCopy copy = bookCopyDAO.findById(copyId);
        if (copy == null) {
            throw new ResourceNotFoundException("Book copy not found: " + copyId);
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            bookCopyDAO.updateStatus(conn, copyId, status);
            if (conditionNotes != null) {
                bookCopyDAO.updateCondition(copyId, conditionNotes.trim());
            }

            auditLogDAO.create(conn, new AuditLog(actorUserId, "COPY_STATUS_UPDATED", "BOOK_COPY", copyId,
                    "Updated copy " + copy.getAccessionNumber() + " status to " + status + " (" + conditionNotes + ")"));

            conn.commit();
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Error updating copy status {}", copyId, e);
            throw new DatabaseException("Failed to update copy status", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public List<BookCopy> getBookCopies(Long bookId) {
        return bookCopyDAO.findCopiesByBookId(bookId);
    }

    public List<Genre> getAllGenres() {
        return genreDAO.findAll();
    }

    public List<Author> getAllAuthors() {
        return authorDAO.findAll();
    }
}
