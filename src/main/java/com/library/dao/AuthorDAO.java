package com.library.dao;

import com.library.model.Author;

import java.sql.Connection;
import java.util.List;

public interface AuthorDAO {
    Author findById(Long id);
    Author findByName(String name);
    List<Author> findAll();
    Long findOrCreate(Connection conn, String name);
    List<Author> findAuthorsByBookId(Long bookId);
    void assignAuthorToBook(Connection conn, Long bookId, Long authorId);
    void clearAuthorsForBook(Connection conn, Long bookId);
}
