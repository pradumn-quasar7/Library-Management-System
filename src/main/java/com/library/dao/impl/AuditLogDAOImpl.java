package com.library.dao.impl;

import com.library.dao.AuditLogDAO;
import com.library.exception.DatabaseException;
import com.library.model.AuditLog;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAOImpl.class);

    private static final String BASE_QUERY =
            "SELECT a.id, a.actor_user_id, a.action, a.entity_type, a.entity_id, a.description, a.created_at, " +
            "u.email AS actor_email, u.role AS actor_role " +
            "FROM audit_logs a " +
            "LEFT JOIN users u ON a.actor_user_id = u.id ";

    @Override
    public Long create(AuditLog log) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return create(conn, log);
        } catch (SQLException e) {
            logger.error("Error creating audit log", e);
            throw new DatabaseException("Failed to create audit log", e);
        }
    }

    @Override
    public Long create(Connection conn, AuditLog log) {
        String sql = "INSERT INTO audit_logs (actor_user_id, action, entity_type, entity_id, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getActorUserId() != null) {
                ps.setLong(1, log.getActorUserId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityType());
            if (log.getEntityId() != null) {
                ps.setLong(4, log.getEntityId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setString(5, log.getDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        log.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to insert audit log, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error inserting audit log in transaction", e);
            throw new DatabaseException("Failed to record audit log", e);
        }
    }

    @Override
    public List<AuditLog> findRecentLogs(int limit) {
        String sql = BASE_QUERY + "ORDER BY a.created_at DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving recent audit logs", e);
            throw new DatabaseException("Failed to list audit logs", e);
        }
    }

    @Override
    public List<AuditLog> findByEntityType(String entityType, Long entityId) {
        String sql = BASE_QUERY + "WHERE a.entity_type = ? AND a.entity_id = ? ORDER BY a.created_at DESC";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entityType);
            ps.setLong(2, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding audit logs for entity: {} {}", entityType, entityId, e);
            throw new DatabaseException("Failed to find entity audit logs", e);
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));
        long actorId = rs.getLong("actor_user_id");
        if (!rs.wasNull()) {
            log.setActorUserId(actorId);
        }
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));
        long entityId = rs.getLong("entity_id");
        if (!rs.wasNull()) {
            log.setEntityId(entityId);
        }
        log.setDescription(rs.getString("description"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        log.setActorEmail(rs.getString("actor_email"));
        log.setActorRole(rs.getString("actor_role"));
        return log;
    }
}
