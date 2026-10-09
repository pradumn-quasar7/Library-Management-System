<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Fines — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Library Fines &amp; Penalties</h1>
        <p class="subtitle">Outstanding overdue penalties and fee settlements</p>
    </div>
    <div class="page-header-actions">
        <div class="stat-card danger" style="padding: 0.5rem 1rem; border-radius: 8px;">
            <div class="stat-info">
                <div class="stat-label">Total Outstanding</div>
                <div class="stat-value" style="font-size: 1.25rem;">$${totalUnpaid}</div>
            </div>
        </div>
    </div>
</div>

<div class="search-filter-bar">
    <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
        <span style="font-weight: 600; font-size: 0.88rem; margin-right: 0.5rem;">Filter by Status:</span>
        <a href="${pageContext.request.contextPath}/librarian/fines?status=ALL" class="btn ${currentFilter eq 'ALL' ? 'btn-primary' : 'btn-secondary'} btn-sm">All</a>
        <a href="${pageContext.request.contextPath}/librarian/fines?status=UNPAID" class="btn ${currentFilter eq 'UNPAID' ? 'btn-danger' : 'btn-secondary'} btn-sm">Unpaid</a>
        <a href="${pageContext.request.contextPath}/librarian/fines?status=PAID" class="btn ${currentFilter eq 'PAID' ? 'btn-success' : 'btn-secondary'} btn-sm">Paid</a>
        <a href="${pageContext.request.contextPath}/librarian/fines?status=WAIVED" class="btn ${currentFilter eq 'WAIVED' ? 'btn-secondary' : 'btn-secondary'} btn-sm">Waived</a>
    </div>
</div>

<div class="table-responsive">
    <table class="table">
        <thead>
            <tr>
                <th>Fine ID</th>
                <th>Member</th>
                <th>Book &amp; Loan</th>
                <th>Amount</th>
                <th>Reason</th>
                <th>Assessed Date</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty fines}">
                    <c:forEach var="f" items="${fines}">
                        <tr>
                            <td><code>#${f.id}</code></td>
                            <td>
                                <strong>${f.memberName}</strong><br/>
                                <small style="color: var(--text-muted);">${f.memberMembershipId}</small>
                            </td>
                            <td>
                                <strong>${f.bookTitle}</strong><br/>
                                <small style="color: var(--text-muted);">Loan #${f.loanId} &bull; Copy: <code>${f.accessionNumber}</code></small>
                            </td>
                            <td><strong style="color: var(--danger); font-size: 1.05rem;">$${f.amount}</strong></td>
                            <td>${f.reason}</td>
                            <td>${f.createdAt}</td>
                            <td>
                                <span class="badge ${f.status eq 'UNPAID' ? 'badge-danger' : (f.status eq 'PAID' ? 'badge-success' : 'badge-secondary')}">
                                    ${f.status}
                                </span>
                            </td>
                            <td>
                                <c:if test="${f.status eq 'UNPAID'}">
                                    <form action="${pageContext.request.contextPath}/librarian/fines" method="POST" onsubmit="return confirm('Waive this fine for member?');">
                                        <input type="hidden" name="action" value="waive"/>
                                        <input type="hidden" name="fineId" value="${f.id}"/>
                                        <button type="submit" class="btn btn-secondary btn-sm">Waive Fine</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr><td colspan="8" class="empty-state">No fine records found.</td></tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
