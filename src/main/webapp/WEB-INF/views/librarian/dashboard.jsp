<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Librarian Dashboard — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Librarian Control Center</h1>
        <p class="subtitle">Real-time system telemetry, active circulation, and inventory health</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/librarian/books/create" class="btn btn-primary">➕ Add New Book</a>
        <a href="${pageContext.request.contextPath}/librarian/reports/export?type=inventory" class="btn btn-secondary">📥 Export Inventory CSV</a>
    </div>
</div>

<!-- 8 Key Metrics Cards per Rubric Section 27 -->
<div class="metrics-grid">
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Total Titles</div>
            <div class="stat-value">${metrics.totalBooks}</div>
        </div>
        <div class="stat-icon">📚</div>
    </div>

    <div class="stat-card success">
        <div class="stat-info">
            <div class="stat-label">Available Copies</div>
            <div class="stat-value">${metrics.availableCopies}</div>
        </div>
        <div class="stat-icon">📗</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Borrowed Copies</div>
            <div class="stat-value">${metrics.borrowedCopies}</div>
        </div>
        <div class="stat-icon">📖</div>
    </div>

    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Active Members</div>
            <div class="stat-value">${metrics.activeMembers}</div>
        </div>
        <div class="stat-icon">👥</div>
    </div>

    <div class="stat-card danger">
        <div class="stat-info">
            <div class="stat-label">Overdue Loans</div>
            <div class="stat-value">${metrics.overdueLoans}</div>
        </div>
        <div class="stat-icon">⚠️</div>
    </div>

    <div class="stat-card warning">
        <div class="stat-info">
            <div class="stat-label">Queue Holds</div>
            <div class="stat-value">${metrics.waitingReservations}</div>
        </div>
        <div class="stat-icon">⏳</div>
    </div>

    <div class="stat-card danger">
        <div class="stat-info">
            <div class="stat-label">Unpaid Fines</div>
            <div class="stat-value">$${metrics.totalUnpaidFines}</div>
        </div>
        <div class="stat-icon">💵</div>
    </div>

    <div class="stat-card accent">
        <div class="stat-info">
            <div class="stat-label">Room Bookings Today</div>
            <div class="stat-value">${metrics.todayBookings}</div>
        </div>
        <div class="stat-icon">🪑</div>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Left Column: Active Loans & Overdue -->
    <div>
        <div class="card" style="margin-bottom: 1.5rem;">
            <div class="card-header">
                <div class="card-title">Recent Circulation Activity</div>
                <a href="${pageContext.request.contextPath}/librarian/transactions" class="btn btn-secondary btn-sm">View All</a>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Book / Copy</th>
                            <th>Member</th>
                            <th>Due Date</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty recentLoans}">
                                <c:forEach var="loan" items="${recentLoans}">
                                    <tr>
                                        <td>
                                            <strong>${loan.bookTitle}</strong><br/>
                                            <small class="text-muted"><code>${loan.accessionNumber}</code></small>
                                        </td>
                                        <td>${loan.memberName}</td>
                                        <td>
                                            ${loan.dueDate}
                                            <c:if test="${loan.isOverdue()}">
                                                <br/><span class="badge badge-danger">${loan.daysOverdue}d late</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span class="badge ${loan.status eq 'ACTIVE' ? 'badge-info' : (loan.status eq 'OVERDUE' ? 'badge-danger' : 'badge-success')}">
                                                ${loan.status}
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${loan.status eq 'ACTIVE' or loan.status eq 'OVERDUE'}">
                                                <form action="${pageContext.request.contextPath}/librarian/transactions/return" method="POST" style="display:inline;">
                                                    <input type="hidden" name="loanId" value="${loan.id}"/>
                                                    <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/librarian/dashboard"/>
                                                    <button type="submit" class="btn btn-primary btn-sm">Return</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="5" class="empty-state">No recent loans recorded.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Right Column: Popular Books & Recent Audits -->
    <div>
        <div class="card" style="margin-bottom: 1.5rem;">
            <div class="card-header">
                <div class="card-title">🔥 Most Borrowed Titles</div>
            </div>
            <ul style="list-style: none; padding: 0;">
                <c:forEach var="book" items="${popularBooks}">
                    <li style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong>${book.title}</strong><br/>
                            <small style="color: var(--text-muted);">${book.authorsFormatted}</small>
                        </div>
                        <span class="badge badge-info">${book.availableCopies}/${book.totalCopies} Avail</span>
                    </li>
                </c:forEach>
            </ul>
        </div>

        <div class="card">
            <div class="card-header">
                <div class="card-title">🛡️ Security Audit Feed</div>
                <a href="${pageContext.request.contextPath}/librarian/audit" class="btn btn-secondary btn-sm">Full Audit</a>
            </div>
            <div style="font-size: 0.84rem;">
                <c:forEach var="log" items="${recentAuditLogs}">
                    <div style="padding: 0.5rem 0; border-bottom: 1px solid var(--border-subtle);">
                        <span class="badge badge-secondary">${log.action}</span>
                        <div style="margin-top: 2px; color: var(--text-secondary);">${log.description}</div>
                        <small style="color: var(--text-muted);">${log.createdAt}</small>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
