<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reports & Analytics — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Library Reports &amp; Analytics</h1>
        <p class="subtitle">Real-time inventory metrics, borrowing trends, and data exports</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/librarian/reports/export?type=inventory" class="btn btn-primary">📥 Export Inventory CSV</a>
        <a href="${pageContext.request.contextPath}/librarian/reports/export?type=overdue" class="btn btn-secondary">📥 Export Overdue Loans CSV</a>
    </div>
</div>

<div class="metrics-grid">
    <div class="stat-card">
        <div class="stat-info">
            <div class="stat-label">Catalog Titles</div>
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
    <div class="stat-card warning">
        <div class="stat-info">
            <div class="stat-label">Borrowed Copies</div>
            <div class="stat-value">${metrics.borrowedCopies}</div>
        </div>
        <div class="stat-icon">📖</div>
    </div>
    <div class="stat-card danger">
        <div class="stat-info">
            <div class="stat-label">Overdue Fines</div>
            <div class="stat-value">$${metrics.totalUnpaidFines}</div>
        </div>
        <div class="stat-icon">💵</div>
    </div>
</div>

<div class="dashboard-grid">
    <div class="card">
        <div class="card-header">
            <div class="card-title">Top 10 Most Borrowed Books</div>
        </div>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Rank</th>
                        <th>Book Title</th>
                        <th>ISBN</th>
                        <th>Available Stock</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="b" items="${popularBooks}" varStatus="status">
                        <tr>
                            <td><strong>#${status.index + 1}</strong></td>
                            <td>
                                <strong>${b.title}</strong><br/>
                                <small style="color: var(--text-muted);">${b.authorsFormatted}</small>
                            </td>
                            <td><code>${b.isbn}</code></td>
                            <td><span class="badge badge-info">${b.availableCopies} / ${b.totalCopies} Available</span></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <div class="card-title">Current Overdue Delinquencies (${overdueLoans.size()})</div>
        </div>
        <ul style="list-style: none; padding: 0;">
            <c:choose>
                <c:when test="${not empty overdueLoans}">
                    <c:forEach var="o" items="${overdueLoans}">
                        <li style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-subtle);">
                            <strong>${o.bookTitle}</strong><br/>
                            <small style="color: var(--text-secondary);">Member: ${o.memberName} (${o.memberEmail})</small><br/>
                            <span class="badge badge-danger">${o.daysOverdue} days overdue</span>
                        </li>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <li class="empty-state">No overdue books at this time! Great job!</li>
                </c:otherwise>
            </c:choose>
        </ul>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
