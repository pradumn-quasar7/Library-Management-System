<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Notifications & Alerts — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Notifications &amp; Alerts</h1>
        <p class="subtitle">System notices, due-date reminders, and reservation pickup alerts</p>
    </div>
    <div class="page-header-actions">
        <form action="${pageContext.request.contextPath}/member/notifications/read" method="POST">
            <input type="hidden" name="all" value="true"/>
            <button type="submit" class="btn btn-secondary">Mark All as Read</button>
        </form>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Notifications Feed -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Recent Activity Feed (${notifications.size()})</div>
        </div>

        <ul style="list-style: none; padding: 0;">
            <c:choose>
                <c:when test="${not empty notifications}">
                    <c:forEach var="n" items="${notifications}">
                        <li style="padding: 1rem; border-bottom: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; ${!n.read ? 'background: #f8fafc;' : ''}">
                            <div>
                                <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.25rem;">
                                    <span class="badge ${n.type eq 'OVERDUE' ? 'badge-danger' : (n.type eq 'DUE_SOON' ? 'badge-warning' : (n.type eq 'RESERVATION_READY' ? 'badge-success' : 'badge-info'))}">
                                        ${n.type}
                                    </span>
                                    <strong>${n.title}</strong>
                                    <c:if test="${!n.read}">
                                        <span class="badge badge-info" style="font-size: 0.65rem;">NEW</span>
                                    </c:if>
                                </div>
                                <p style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 0.25rem;">${n.message}</p>
                                <small style="color: var(--text-muted);">${n.createdAt}</small>
                            </div>

                            <c:if test="${!n.read}">
                                <form action="${pageContext.request.contextPath}/member/notifications/read" method="POST">
                                    <input type="hidden" name="id" value="${n.id}"/>
                                    <button type="submit" class="btn btn-secondary btn-sm">Mark Read</button>
                                </form>
                            </c:if>
                        </li>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <li class="empty-state">No notifications recorded yet.</li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>

    <!-- Notification Preferences (Section 33) -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Alert Preferences</div>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1.25rem;">
            Control which automated system alerts you receive:
        </p>

        <form action="${pageContext.request.contextPath}/member/notifications/preferences" method="POST">
            <div class="form-group">
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                    <input type="checkbox" name="dueDateAlert" ${preferences.dueDateAlert ? 'checked' : ''}>
                    <span>Due Date Approaching Reminders</span>
                </label>
            </div>

            <div class="form-group">
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                    <input type="checkbox" name="overdueAlert" ${preferences.overdueAlert ? 'checked' : ''}>
                    <span>Overdue Loan Notices</span>
                </label>
            </div>

            <div class="form-group">
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                    <input type="checkbox" name="reservationAlert" ${preferences.reservationAlert ? 'checked' : ''}>
                    <span>Reservation Ready Pickup Alerts</span>
                </label>
            </div>

            <div class="form-group">
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                    <input type="checkbox" name="newArrivalAlert" ${preferences.newArrivalAlert ? 'checked' : ''}>
                    <span>New Book Arrival Announcements</span>
                </label>
            </div>

            <button type="submit" class="btn btn-primary btn-sm" style="width: 100%; margin-top: 1rem;">Save Alert Preferences</button>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
