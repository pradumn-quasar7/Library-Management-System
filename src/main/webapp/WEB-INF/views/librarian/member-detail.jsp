<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Member Profile — ".concat(member.fullName).concat(" — Athena Library") scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Member Dossier: ${member.fullName}</h1>
        <p class="subtitle">Membership ID: <code>${member.membershipId}</code> &bull; Email: ${member.email}</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/librarian/members" class="btn btn-secondary">← Back to Members</a>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Member Info Card -->
    <div>
        <div class="card" style="margin-bottom: 1.5rem;">
            <div class="card-header">
                <div class="card-title">Profile Information</div>
                <span class="badge ${member.status eq 'ACTIVE' ? 'badge-success' : 'badge-danger'}">${member.status}</span>
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; font-size: 0.92rem;">
                <div><strong>Full Name:</strong> ${member.fullName}</div>
                <div><strong>Membership ID:</strong> <code>${member.membershipId}</code></div>
                <div><strong>Email:</strong> ${member.email}</div>
                <div><strong>Phone:</strong> ${member.phone != null ? member.phone : "—"}</div>
                <div><strong>Address:</strong> ${member.address != null ? member.address : "—"}</div>
                <div><strong>Member Since:</strong> ${member.joinedAt}</div>
            </div>
        </div>

        <!-- Active Loans -->
        <div class="card">
            <div class="card-header">
                <div class="card-title">Currently Borrowed Books (${activeLoans.size()})</div>
            </div>
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Book Title</th>
                            <th>Copy Barcode</th>
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
                                        <td><strong>${l.bookTitle}</strong></td>
                                        <td><code>${l.accessionNumber}</code></td>
                                        <td>
                                            ${l.dueDate}
                                            <c:if test="${l.isOverdue()}">
                                                <br/><span class="badge badge-danger">${l.daysOverdue}d late</span>
                                            </c:if>
                                        </td>
                                        <td><span class="badge ${l.status eq 'ACTIVE' ? 'badge-info' : 'badge-danger'}">${l.status}</span></td>
                                        <td>
                                            <form action="${pageContext.request.contextPath}/librarian/transactions/return" method="POST">
                                                <input type="hidden" name="loanId" value="${l.id}"/>
                                                <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/librarian/members?id=${member.id}"/>
                                                <button type="submit" class="btn btn-primary btn-sm">Return</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="5" class="empty-state">No active loans for this member.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Loan History -->
    <div>
        <div class="card">
            <div class="card-header">
                <div class="card-title">Historical Transactions (${history.size()})</div>
            </div>
            <ul style="list-style: none; padding: 0;">
                <c:forEach var="h" items="${history}">
                    <li style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-subtle);">
                        <strong>${h.bookTitle}</strong><br/>
                        <small style="color: var(--text-secondary);">
                            Borrowed: ${h.borrowedAt} &bull; Status: <span class="badge badge-secondary">${h.status}</span>
                        </small>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
