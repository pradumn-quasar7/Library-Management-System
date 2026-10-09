package com.library.service;

import com.library.dao.AuditLogDAO;
import com.library.dao.impl.AuditLogDAOImpl;
import com.library.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogDAO auditLogDAO;

    public AuditService() {
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public AuditService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    public void log(Long actorUserId, String action, String entityType, Long entityId, String description) {
        AuditLog entry = new AuditLog(actorUserId, action, entityType, entityId, description);
        auditLogDAO.create(entry);
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.findRecentLogs(limit);
    }

    public List<AuditLog> getLogsForEntity(String entityType, Long entityId) {
        return auditLogDAO.findByEntityType(entityType, entityId);
    }
}
