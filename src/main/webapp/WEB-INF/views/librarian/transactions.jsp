<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Circulation Transactions — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Circulation &amp; Transactions Log</h1>
        <p class="subtitle">Real-time circulation log across all physical book copies and members</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/librarian/reports/export?type=overdue" class="btn btn-secondary">📥 Export Overdue CSV</a>
    </div>
</div>

<div class="search-filter-bar">
    <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
        <span style="font-weight: 600; font-size: 0.88rem; margin-right: 0.5rem;">Filter by Status:</span>
        <a href="${pageContext.request.contextPath}/librarian/transactions?status=ALL" class="btn ${currentFilter eq 'ALL' ? 'btn-primary' : 'btn-secondary'} btn-sm">All</a>
        <a href="${pageContext.request.contextPath}/librarian/transactions?status=ACTIVE" class="btn ${currentFilter eq 'ACTIVE' ? 'btn-primary' : 'btn-secondary'} btn-sm">Active</a>
        <a href="${pageContext.request.contextPath}/librarian/transactions?status=OVERDUE" class="btn ${currentFilter eq 'OVERDUE' ? 'btn-danger' : 'btn-secondary'} btn-sm">Overdue</a>
        <a href="${pageContext.request.contextPath}/librarian/transactions?status=RETURNED" class="btn ${currentFilter eq 'RETURNED' ? 'btn-success' : 'btn-secondary'} btn-sm">Returned</a>
    </div>
</div>

<div class="table-responsive">
    <table class="table">
        <thead>
            <tr>
                <th>Loan ID</th>
                <th>Book Title &amp; Copy</th>
                <th>Member</th>
                <th>Borrowed Date</th>
                <th>Due Date</th>
                <th>Returned Date</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty loans}">
                    <c:forEach var="l" items="${loans}">
                        <tr>
                            <td><code>#${l.id}</code></td>
                            <td>
                                <strong>${l.bookTitle}</strong><br/>
                                <small style="color: var(--text-muted);">Accession: <code>${l.accessionNumber}</code></small>
                            </td>
                            <td>
                                <strong>${l.memberName}</strong><br/>
                                <small style="color: var(--text-muted);">${l.memberEmail}</small>
                            </td>
                            <td>${l.borrowedAt}</td>
                            <td>
                                ${l.dueDate}
                                <c:if test="${l.isOverdue()}">
                                    <br/><span class="badge badge-danger">${l.daysOverdue} days late</span>
                                </c:if>
                            </td>
                            <td>${l.returnedAt != null ? l.returnedAt : "—"}</td>
                            <td>
                                <span class="badge ${l.status eq 'ACTIVE' ? 'badge-info' : (l.status eq 'OVERDUE' ? 'badge-danger' : 'badge-success')}">
                                    ${l.status}
                                </span>
                            </td>
                            <td>
                                <c:if test="${l.status eq 'ACTIVE' or l.status eq 'OVERDUE'}">
                                    <div style="display: flex; gap: 4px;">
                                        <form action="${pageContext.request.contextPath}/librarian/transactions/return" method="POST">
                                            <input type="hidden" name="loanId" value="${l.id}"/>
                                            <button type="submit" class="btn btn-primary btn-sm">Check In</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/librarian/transactions/renew" method="POST">
                                            <input type="hidden" name="loanId" value="${l.id}"/>
                                            <button type="submit" class="btn btn-secondary btn-sm" ${l.isOverdue() ? 'disabled' : ''}>Renew</button>
                                        </form>
                                    </div>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr><td colspan="8" class="empty-state">No transaction records found.</td></tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
