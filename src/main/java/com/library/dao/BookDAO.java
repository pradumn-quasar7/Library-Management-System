package com.library.dao;

import com.library.model.Book;

import java.sql.Connection;
import java.util.List;

public interface BookDAO {
    Book findById(Long id);
    Book findByIsbn(String isbn);
    List<Book> findAll(boolean activeOnly);
    List<Book> search(String query, Long genreId, Boolean availableOnly, String sortBy);
    Long create(Connection conn, Book book);
    boolean update(Connection conn, Book book);
    boolean setStatus(Long id, String status);
    int countTotalBooks();
    int countAvailableBooks();
    List<Book> findTopPopularBooks(int limit);
    List<Book> findRecentlyAddedBooks(int limit);
}
