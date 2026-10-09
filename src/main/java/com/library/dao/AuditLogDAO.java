package com.library.dao;

import com.library.model.AuditLog;

import java.sql.Connection;
import java.util.List;

public interface AuditLogDAO {
    Long create(Connection conn, AuditLog log);
    Long create(AuditLog log);
    List<AuditLog> findRecentLogs(int limit);
    List<AuditLog> findByEntityType(String entityType, Long entityId);
}
