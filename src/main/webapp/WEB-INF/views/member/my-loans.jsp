<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Active Loans — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>My Active Borrowed Books</h1>
        <p class="subtitle">Monitor return dates, manage renewals, and check in borrowed books</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/books/search" class="btn btn-primary">➕ Borrow More Books</a>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th>Book Title</th>
                    <th>Copy Barcode</th>
                    <th>Borrowed Date</th>
                    <th>Due Date</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty activeLoans}">
                        <c:forEach var="l" items="${activeLoans}">
                            <tr>
                                <td>
                                    <strong>${l.bookTitle}</strong><br/>
                                    <small style="color: var(--text-muted);">ISBN: <code>${l.bookIsbn}</code></small>
                                </td>
                                <td><code>${l.accessionNumber}</code></td>
                                <td>${l.borrowedAt}</td>
                                <td>
                                    <strong>${l.dueDate}</strong>
                                    <c:choose>
                                        <c:when test="${l.isOverdue()}">
                                            <br/><span class="badge badge-danger">${l.daysOverdue} days overdue</span>
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
                                    <div style="display: flex; gap: 0.5rem;">
                                        <form action="${pageContext.request.contextPath}/member/return" method="POST">
                                            <input type="hidden" name="loanId" value="${l.id}"/>
                                            <button type="submit" class="btn btn-primary btn-sm">Return Book</button>
                                        </form>

                                        <form action="${pageContext.request.contextPath}/member/renew" method="POST">
                                            <input type="hidden" name="loanId" value="${l.id}"/>
                                            <button type="submit" class="btn btn-secondary btn-sm"
                                                    ${l.isOverdue() or l.renewalCount >= 2 ? 'disabled' : ''}>
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
                            <td colspan="6" class="empty-state">
                                <div class="empty-state-icon">📖</div>
                                <h3>You have no active loans right now</h3>
                                <p>Ready to read? Check out our catalog of books available for borrowing.</p>
                                <a href="${pageContext.request.contextPath}/books/search" class="btn btn-primary" style="margin-top: 1rem;">Browse Catalog</a>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
