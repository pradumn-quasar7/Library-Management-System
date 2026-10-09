<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Borrowing History — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Borrowing History</h1>
        <p class="subtitle">Complete chronological record of all your past library loans</p>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th>Book Title</th>
                    <th>Copy Accession</th>
                    <th>Borrowed At</th>
                    <th>Due Date</th>
                    <th>Returned At</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty history}">
                        <c:forEach var="h" items="${history}">
                            <tr>
                                <td>
                                    <strong>${h.bookTitle}</strong><br/>
                                    <small style="color: var(--text-muted);">ISBN: <code>${h.bookIsbn}</code></small>
                                </td>
                                <td><code>${h.accessionNumber}</code></td>
                                <td>${h.borrowedAt}</td>
                                <td>${h.dueDate}</td>
                                <td>${h.returnedAt != null ? h.returnedAt : "Not returned yet"}</td>
                                <td>
                                    <span class="badge ${h.status eq 'RETURNED' ? 'badge-success' : (h.status eq 'ACTIVE' ? 'badge-info' : 'badge-danger')}">
                                        ${h.status}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="6" class="empty-state">No transaction history recorded yet.</td></tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
