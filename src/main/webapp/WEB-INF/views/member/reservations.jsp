<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Book Reservations — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>My Book Reservations &amp; Queue</h1>
        <p class="subtitle">Track your waiting positions and pending pickup holds</p>
    </div>
</div>

<div class="card">
    <div class="table-responsive">
        <table class="table">
            <thead>
                <tr>
                    <th>Title &amp; ISBN</th>
                    <th>Reserved Date</th>
                    <th>Queue Position</th>
                    <th>Hold Status</th>
                    <th>Hold Expiry</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty reservations}">
                        <c:forEach var="r" items="${reservations}">
                            <tr>
                                <td>
                                    <strong>${r.bookTitle}</strong><br/>
                                    <small style="color: var(--text-muted);">ISBN: <code>${r.bookIsbn}</code></small>
                                </td>
                                <td>${r.reservedAt}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${r.status eq 'READY'}">
                                            <span class="badge badge-success">READY FOR PICKUP</span>
                                        </c:when>
                                        <c:when test="${r.status eq 'WAITING'}">
                                            <span class="badge badge-warning">Queue Position #${r.queuePosition}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-secondary">${r.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge ${r.status eq 'READY' ? 'badge-success' : (r.status eq 'WAITING' ? 'badge-warning' : 'badge-secondary')}">
                                        ${r.status}
                                    </span>
                                </td>
                                <td>${r.expiresAt != null ? r.expiresAt : "—"}</td>
                                <td>
                                    <c:if test="${r.status eq 'WAITING' or r.status eq 'READY'}">
                                        <form action="${pageContext.request.contextPath}/member/reserve/cancel" method="POST" onsubmit="return confirm('Cancel this reservation?');">
                                            <input type="hidden" name="reservationId" value="${r.id}"/>
                                            <button type="submit" class="btn btn-danger btn-sm">Cancel</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="empty-state">
                                <div class="empty-state-icon">⏳</div>
                                <h3>No active reservations</h3>
                                <p>When a book has zero available copies, you can join the waiting queue to get notified first!</p>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
