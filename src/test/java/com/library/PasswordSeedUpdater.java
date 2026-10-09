package com.library;

import com.library.util.ConnectionManager;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PasswordSeedUpdater {

    @Test
    public void updateSeedPasswords() throws Exception {
        String adminHash = BCrypt.hashpw("Admin@123", BCrypt.gensalt(10));
        String memberHash = BCrypt.hashpw("Member@123", BCrypt.gensalt(10));

        System.out.println("Admin Hash: " + adminHash);
        System.out.println("Member Hash: " + memberHash);

        try (Connection conn = ConnectionManager.getConnection()) {
            // Update admin
            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE role = 'LIBRARIAN'")) {
                ps.setString(1, adminHash);
                ps.executeUpdate();
            }

            // Update members
            try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE role = 'MEMBER'")) {
                ps.setString(1, memberHash);
                ps.executeUpdate();
            }
        }
        System.out.println("Passwords in library_db successfully updated to valid BCrypt hashes!");
    }
}
