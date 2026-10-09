package com.library.dao.impl;

import com.library.dao.NotificationDAO;
import com.library.exception.DatabaseException;
import com.library.model.Notification;
import com.library.model.NotificationPreference;
import com.library.model.NotificationType;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDAOImpl.class);

    @Override
    public Notification findById(Long id) {
        String sql = "SELECT id, member_id, title, message, type, is_read, created_at FROM notifications WHERE id = ?";
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
            logger.error("Error finding notification by id: {}", id, e);
            throw new DatabaseException("Failed to find notification", e);
        }
    }

    @Override
    public List<Notification> findByMemberId(Long memberId, int limit) {
        String sql = "SELECT id, member_id, title, message, type, is_read, created_at FROM notifications WHERE member_id = ? ORDER BY created_at DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding notifications for member: {}", memberId, e);
            throw new DatabaseException("Failed to list notifications", e);
        }
    }

    @Override
    public int countUnreadByMemberId(Long memberId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE member_id = ? AND is_read = FALSE";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting unread notifications for member: {}", memberId, e);
            throw new DatabaseException("Failed to count unread notifications", e);
        }
    }

    @Override
    public Long create(Notification notification) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return create(conn, notification);
        } catch (SQLException e) {
            logger.error("Error creating notification", e);
            throw new DatabaseException("Failed to create notification", e);
        }
    }

    @Override
    public Long create(Connection conn, Notification notification) {
        String sql = "INSERT INTO notifications (member_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, notification.getMemberId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType() != null ? notification.getType().name() : NotificationType.GENERAL.name());
            ps.setBoolean(5, notification.isRead());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        notification.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to create notification, no ID generated.");
        } catch (SQLException e) {
            logger.error("Error inserting notification", e);
            throw new DatabaseException("Failed to record notification", e);
        }
    }

    @Override
    public boolean markAsRead(Long id, Long memberId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND member_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking notification {} as read", id, e);
            throw new DatabaseException("Failed to mark notification as read", e);
        }
    }

    @Override
    public boolean markAllAsRead(Long memberId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE member_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Error marking all notifications as read for member: {}", memberId, e);
            throw new DatabaseException("Failed to mark all notifications as read", e);
        }
    }

    @Override
    public NotificationPreference findPreferencesByMemberId(Long memberId) {
        String sql = "SELECT member_id, due_date_alert, overdue_alert, new_arrival_alert, reservation_alert FROM notification_preferences WHERE member_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NotificationPreference pref = new NotificationPreference();
                    pref.setMemberId(rs.getLong("member_id"));
                    pref.setDueDateAlert(rs.getBoolean("due_date_alert"));
                    pref.setOverdueAlert(rs.getBoolean("overdue_alert"));
                    pref.setNewArrivalAlert(rs.getBoolean("new_arrival_alert"));
                    pref.setReservationAlert(rs.getBoolean("reservation_alert"));
                    return pref;
                }
            }
            return new NotificationPreference(memberId, true, true, true, true);
        } catch (SQLException e) {
            logger.error("Error retrieving notification preferences for member: {}", memberId, e);
            throw new DatabaseException("Failed to fetch notification preferences", e);
        }
    }

    @Override
    public boolean saveOrUpdatePreferences(NotificationPreference pref) {
        String sql = "INSERT INTO notification_preferences (member_id, due_date_alert, overdue_alert, new_arrival_alert, reservation_alert) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "due_date_alert = VALUES(due_date_alert), " +
                "overdue_alert = VALUES(overdue_alert), " +
                "new_arrival_alert = VALUES(new_arrival_alert), " +
                "reservation_alert = VALUES(reservation_alert)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pref.getMemberId());
            ps.setBoolean(2, pref.isDueDateAlert());
            ps.setBoolean(3, pref.isOverdueAlert());
            ps.setBoolean(4, pref.isNewArrivalAlert());
            ps.setBoolean(5, pref.isReservationAlert());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving notification preferences for member: {}", pref.getMemberId(), e);
            throw new DatabaseException("Failed to save preferences", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setMemberId(rs.getLong("member_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(NotificationType.fromString(rs.getString("type")));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}
