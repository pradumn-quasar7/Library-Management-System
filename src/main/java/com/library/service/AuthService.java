package com.library.service;

import com.library.dao.AuditLogDAO;
import com.library.dao.MemberDAO;
import com.library.dao.NotificationDAO;
import com.library.dao.UserDAO;
import com.library.dao.impl.AuditLogDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.NotificationDAOImpl;
import com.library.dao.impl.UserDAOImpl;
import com.library.exception.AuthenticationException;
import com.library.exception.ConflictException;
import com.library.exception.DatabaseException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.util.ConnectionManager;
import com.library.validation.EmailValidator;
import com.library.validation.MemberValidator;
import com.library.validation.PasswordValidator;
import com.library.validation.ValidationResult;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDAO;
    private final MemberDAO memberDAO;
    private final NotificationDAO notificationDAO;
    private final AuditLogDAO auditLogDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public AuthService(UserDAO userDAO, MemberDAO memberDAO, NotificationDAO notificationDAO, AuditLogDAO auditLogDAO) {
        this.userDAO = userDAO;
        this.memberDAO = memberDAO;
        this.notificationDAO = notificationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public User authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Email and password are required.");
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            logger.warn("Authentication failed: User not found for email {}", email);
            throw new AuthenticationException("Invalid email or password.");
        }

        if (UserStatus.BLOCKED.equals(user.getStatus())) {
            logger.warn("Authentication failed: Account blocked for user {}", email);
            throw new AuthenticationException("Your account is blocked. Please contact the library administration.");
        }

        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            logger.warn("Authentication failed: Inactive account for user {}", email);
            throw new AuthenticationException("Your account is currently inactive.");
        }

        boolean passwordMatches = false;
        try {
            passwordMatches = BCrypt.checkpw(password, user.getPasswordHash());
        } catch (Exception e) {
            logger.error("Error verifying password hash for user {}", email, e);
            throw new AuthenticationException("Invalid email or password.");
        }

        if (!passwordMatches) {
            logger.warn("Authentication failed: Password mismatch for user {}", email);
            throw new AuthenticationException("Invalid email or password.");
        }

        logger.info("User {} logged in successfully as {}", user.getEmail(), user.getRole());
        auditLogDAO.create(new AuditLog(user.getId(), "LOGIN_SUCCESS", "USER", user.getId(), "User logged in successfully"));
        return user;
    }

    public Member registerMember(String email, String password, String fullName, String phone, String address) {
        Member tempMember = new Member();
        tempMember.setFullName(fullName);
        tempMember.setPhone(phone);
        tempMember.setAddress(address);

        ValidationResult vr = MemberValidator.validate(tempMember, email, password, true);
        if (!vr.isValid()) {
            throw new ValidationException(vr.getErrors());
        }

        if (userDAO.findByEmail(email) != null) {
            throw new ConflictException("An account with email " + email + " already exists.");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        String membershipId = "LIB-MEM-" + (System.currentTimeMillis() % 1000000);

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            User user = new User();
            user.setEmail(email.trim().toLowerCase());
            user.setPasswordHash(passwordHash);
            user.setRole(Role.MEMBER);
            user.setStatus(UserStatus.ACTIVE);
            Long userId = userDAO.create(conn, user);

            Member member = new Member();
            member.setUserId(userId);
            member.setMembershipId(membershipId);
            member.setFullName(fullName.trim());
            member.setPhone(phone != null ? phone.trim() : null);
            member.setAddress(address != null ? address.trim() : null);
            member.setJoinedAt(LocalDate.now());

            Long memberId = memberDAO.create(conn, member);
            member.setId(memberId);
            member.setEmail(email.trim().toLowerCase());
            member.setStatus(UserStatus.ACTIVE);

            // Default preferences
            notificationDAO.saveOrUpdatePreferences(new NotificationPreference(memberId, true, true, true, true));

            // Welcome notification
            notificationDAO.create(conn, new Notification(
                    memberId,
                    "Welcome to Online Library!",
                    "Your membership account (" + membershipId + ") is now active. Explore our catalog and reserve books anytime.",
                    NotificationType.GENERAL
            ));

            // Audit
            auditLogDAO.create(conn, new AuditLog(userId, "MEMBER_REGISTERED", "MEMBER", memberId, "New member registered: " + fullName));

            conn.commit();
            logger.info("Successfully registered new member {} (ID: {})", fullName, membershipId);
            return member;
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Transaction failed during member registration", e);
            throw new DatabaseException("Failed to register member due to database error", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        if (!PasswordValidator.isValid(newPassword)) {
            throw new ValidationException("New password must be at least 6 characters long.");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new AuthenticationException("User not found.");
        }

        if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect.");
        }

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        userDAO.updatePassword(userId, newHash);
        auditLogDAO.create(new AuditLog(userId, "PASSWORD_CHANGED", "USER", userId, "User changed password"));
        logger.info("Password updated successfully for user ID {}", userId);
    }
}
