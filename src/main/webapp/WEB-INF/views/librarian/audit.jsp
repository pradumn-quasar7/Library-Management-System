<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Security Audit Trail — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>System Security &amp; Audit Trail</h1>
        <p class="subtitle">Immutable chronological log of all sensitive user and catalog operations</p>
    </div>
</div>

<div class="table-responsive">
    <table class="table">
        <thead>
            <tr>
                <th>Log ID</th>
                <th>Action</th>
                <th>Actor User</th>
                <th>Entity Affected</th>
                <th>Description</th>
                <th>Timestamp</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty auditLogs}">
                    <c:forEach var="a" items="${auditLogs}">
                        <tr>
                            <td><code>#${a.id}</code></td>
                            <td><span class="badge badge-secondary">${a.action}</span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty a.actorEmail}">
                                        <strong>${a.actorEmail}</strong>
                                        <c:if test="${not empty a.actorRole}">
                                            <span class="badge ${a.actorRole eq 'LIBRARIAN' ? 'badge-danger' : 'badge-info'}" style="margin-left: 4px;">
                                                ${a.actorRole}
                                            </span>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: var(--text-muted);">System / Anonymous</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <code>${a.entityType != null ? a.entityType : "—"} ${a.entityId != null ? '#'.concat(a.entityId) : ""}</code>
                            </td>
                            <td>${a.description}</td>
                            <td><small style="color: var(--text-muted);">${a.createdAt}</small></td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr><td colspan="6" class="empty-state">No audit logs recorded yet.</td></tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
