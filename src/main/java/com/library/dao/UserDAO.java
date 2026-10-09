package com.library.dao;

import com.library.model.User;
import com.library.model.UserStatus;

import java.sql.Connection;
import java.util.List;

public interface UserDAO {
    User findById(Long id);
    User findByEmail(String email);
    Long create(User user);
    Long create(Connection conn, User user);
    boolean update(User user);
    boolean updatePassword(Long id, String passwordHash);
    boolean updateStatus(Long id, UserStatus status);
    int countActiveUsers();
    List<User> findAll();
}
