<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Member Dashboard — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Welcome, ${sessionScope.currentMember.fullName}</h1>
        <p class="subtitle">Member Card: <code>${sessionScope.currentMember.membershipId}</code> &bull; Email: ${sessionScope.currentUser.email}</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/books/search" class="btn btn-primary">🔍 Discover Books</a>
        <a href="${pageContext.request.contextPath}/member/reading-room" class="btn btn-secondary">🪑 Book Reading Room</a>
    </div>
</div>

<!-- Section 7.3: Overdue Risk Indicator -->
<div class="risk-banner ${riskStatus}">
    <div>
        <c:choose>
            <c:when test="${riskStatus eq 'OVERDUE'}">
                ⚠️ <strong>Account Status: OVERDUE</strong> — You have one or more overdue loans. Please return them to prevent accumulating fines.
            </c:when>
            <c:when test="${riskStatus eq 'ATTENTION'}">
                ⏰ <strong>Account Status: ATTENTION</strong> — A book is due within the next 48 hours. Please renew or return it soon.
            </c:when>
            <c:otherwise>
                ✨ <strong>Account Status: In Good Standing (NORMAL)</strong> — All loans are within permitted loan periods.
            </c:otherwise>
        </c:choose>
    </div>
    <span class="badge ${riskStatus eq 'OVERDUE' ? 'badge-danger' : (riskStatus eq 'ATTENTION' ? 'badge-warning' : 'badge-success')}">
        ${riskStatus}
    </span>
</div>

<div class="metrics-grid">
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Active Loans</div>
            <div class="stat-value">${activeLoans.size()} / 5</div>
        </div>
        <div class="stat-icon">📖</div>
    </div>

    <div class="stat-card ${unpaidFines > 0 ? 'danger' : 'success'}">
        <div class="stat-info">
            <div class="stat-label">Unpaid Fines</div>
            <div class="stat-value">$${unpaidFines}</div>
        </div>
        <div class="stat-icon">💵</div>
    </div>

    <div class="stat-card warning">
        <div class="stat-info">
            <div class="stat-label">Reservations</div>
            <div class="stat-value">${reservations.size()}</div>
        </div>
        <div class="stat-icon">⏳</div>
    </div>

    <div class="stat-card info">
        <div class="stat-info">
            <div class="stat-label">Unread Alerts</div>
            <div class="stat-value">${unreadCount}</div>
        </div>
        <div class="stat-icon">🔔</div>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Active Loans Table -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Currently Borrowed Books (${activeLoans.size()})</div>
            <a href="${pageContext.request.contextPath}/member/loans" class="btn btn-secondary btn-sm">Manage Loans</a>
        </div>

        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Title</th>
                        <th>Due Date</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty activeLoans}">
                            <c:forEach var="l" items="${activeLoans}">
                                <tr>
                                    <td>
                                        <strong>${l.bookTitle}</strong><br/>
                                        <small style="color: var(--text-muted);">Copy: <code>${l.accessionNumber}</code></small>
                                    </td>
                                    <td>
                                        ${l.dueDate}
                                        <c:choose>
                                            <c:when test="${l.isOverdue()}">
                                                <br/><span class="badge badge-danger">${l.daysOverdue}d late</span>
                                            </c:when>
                                            <c:otherwise>
                                                <br/><small style="color: var(--text-secondary);">${l.daysRemaining} days left</small>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge ${l.status eq 'ACTIVE' ? 'badge-info' : 'badge-danger'}">
                                            ${l.status}
                                        </span>
                                    </td>
                                    <td>
                                        <div style="display: flex; gap: 4px;">
                                            <form action="${pageContext.request.contextPath}/member/return" method="POST">
                                                <input type="hidden" name="loanId" value="${l.id}"/>
                                                <button type="submit" class="btn btn-primary btn-sm">Return</button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/member/renew" method="POST">
                                                <input type="hidden" name="loanId" value="${l.id}"/>
                                                <button type="submit" class="btn btn-secondary btn-sm" ${l.isOverdue() or l.renewalCount >= 2 ? 'disabled' : ''}>
                                                    Renew (${l.renewalCount}/2)
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="4" class="empty-state">
                                    You have no active loans.<br/>
                                    <a href="${pageContext.request.contextPath}/books/search" class="btn btn-primary btn-sm" style="margin-top: 0.5rem;">Explore Books to Borrow</a>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Recommendations Preview -->
    <div>
        <div class="card" style="margin-bottom: 1.5rem;">
            <div class="card-header">
                <div class="card-title">💡 Smart Recommendations</div>
                <a href="${pageContext.request.contextPath}/member/recommendations" class="btn btn-secondary btn-sm">See All</a>
            </div>
            <ul style="list-style: none; padding: 0;">
                <c:forEach var="rec" items="${recommendations}">
                    <li style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong><a href="${pageContext.request.contextPath}/books/view?id=${rec.id}">${rec.title}</a></strong><br/>
                            <small style="color: var(--text-secondary);">${rec.authorsFormatted}</small>
                        </div>
                        <c:choose>
                            <c:when test="${rec.isAvailable()}">
                                <form action="${pageContext.request.contextPath}/member/borrow" method="POST">
                                    <input type="hidden" name="bookId" value="${rec.id}"/>
                                    <button type="submit" class="btn btn-primary btn-sm">Borrow</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/member/reserve" method="POST">
                                    <input type="hidden" name="bookId" value="${rec.id}"/>
                                    <button type="submit" class="btn btn-secondary btn-sm">Reserve</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
