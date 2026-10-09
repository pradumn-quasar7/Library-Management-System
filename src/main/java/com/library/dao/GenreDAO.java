package com.library.dao;

import com.library.model.Genre;

import java.sql.Connection;
import java.util.List;

public interface GenreDAO {
    Genre findById(Long id);
    Genre findByName(String name);
    List<Genre> findAll();
    Long findOrCreate(Connection conn, String name);
}
