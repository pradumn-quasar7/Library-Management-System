<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${book.title} — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>${book.title}</h1>
        <p class="subtitle">By ${book.authorsFormatted} &bull; Genre: ${book.genreName}</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/books/search" class="btn btn-secondary">← Back to Catalog</a>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Left Column: Details -->
    <div>
        <div class="card" style="margin-bottom: 1.5rem;">
            <div class="card-header">
                <div class="card-title">Bibliographic Details</div>
                <span class="badge ${book.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">
                    ${book.availableCopies} / ${book.totalCopies} Available
                </span>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; font-size: 0.92rem; margin-bottom: 1.5rem;">
                <div><strong>ISBN:</strong> <code>${book.isbn}</code></div>
                <div><strong>Genre:</strong> ${book.genreName}</div>
                <div><strong>Publisher:</strong> ${book.publisher != null ? book.publisher : "—"}</div>
                <div><strong>Publication Year:</strong> ${book.publicationYear != null ? book.publicationYear : "—"}</div>
                <div><strong>Authors:</strong> ${book.authorsFormatted}</div>
                <div><strong>Status:</strong> <span class="badge badge-success">${book.status}</span></div>
            </div>

            <h4>Synopsis &amp; Abstract</h4>
            <p style="color: var(--text-secondary); line-height: 1.6; margin-top: 0.5rem;">
                ${book.description != null ? book.description : "No description provided for this title."}
            </p>

            <div style="margin-top: 2rem; display: flex; gap: 1rem;">
                <c:choose>
                    <c:when test="${sessionScope.userRole eq 'MEMBER'}">
                        <c:choose>
                            <c:when test="${book.isAvailable()}">
                                <form action="${pageContext.request.contextPath}/member/borrow" method="POST">
                                    <input type="hidden" name="bookId" value="${book.id}"/>
                                    <button type="submit" class="btn btn-primary" style="padding: 0.75rem 2rem;">Borrow Available Copy</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/member/reserve" method="POST">
                                    <input type="hidden" name="bookId" value="${book.id}"/>
                                    <button type="submit" class="btn btn-secondary" style="padding: 0.75rem 2rem;">Join Reservation Queue</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:when test="${empty sessionScope.currentUser}">
                        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Sign In to Borrow</a>
                    </c:when>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Right Column: Physical Copies Status -->
    <div>
        <div class="card">
            <div class="card-header">
                <div class="card-title">Physical Inventory Copies (${copies.size()})</div>
            </div>
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Accession</th>
                            <th>Status</th>
                            <th>Condition</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="c" items="${copies}">
                            <tr>
                                <td><code>${c.accessionNumber}</code></td>
                                <td>
                                    <span class="badge ${c.status eq 'AVAILABLE' ? 'badge-success' : (c.status eq 'BORROWED' ? 'badge-info' : 'badge-warning')}">
                                        ${c.status}
                                    </span>
                                </td>
                                <td><small style="color: var(--text-secondary);">${c.conditionNotes}</small></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
