<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="navbar">
    <div class="nav-container">
        <a href="${pageContext.request.contextPath}/" class="nav-brand">
            <span class="brand-icon">📚</span>
            <span>Athena<span style="color: var(--primary);">Library</span></span>
        </a>

        <ul class="nav-links">
            <c:choose>
                <c:when test="${sessionScope.userRole eq 'LIBRARIAN'}">
                    <li><a href="${pageContext.request.contextPath}/librarian/dashboard" class="nav-link">Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/books" class="nav-link">Books Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/members" class="nav-link">Members</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/transactions" class="nav-link">Circulation</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/fines" class="nav-link">Fines</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/reading-room" class="nav-link">Reading Room</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/reports" class="nav-link">Reports</a></li>
                    <li><a href="${pageContext.request.contextPath}/librarian/audit" class="nav-link">Audit Trail</a></li>
                </c:when>
                <c:when test="${sessionScope.userRole eq 'MEMBER'}">
                    <li><a href="${pageContext.request.contextPath}/member/dashboard" class="nav-link">Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/books/search" class="nav-link">Catalog</a></li>
                    <li><a href="${pageContext.request.contextPath}/member/loans" class="nav-link">My Loans</a></li>
                    <li><a href="${pageContext.request.contextPath}/member/reserve" class="nav-link">Reservations</a></li>
                    <li><a href="${pageContext.request.contextPath}/member/recommendations" class="nav-link">Recommendations</a></li>
                    <li><a href="${pageContext.request.contextPath}/member/reading-room" class="nav-link">Reading Room</a></li>
                    <li><a href="${pageContext.request.contextPath}/member/history" class="nav-link">History</a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="${pageContext.request.contextPath}/" class="nav-link">Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/books/search" class="nav-link">Catalog</a></li>
                </c:otherwise>
            </c:choose>
        </ul>

        <div class="nav-user">
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <c:if test="${sessionScope.userRole eq 'MEMBER'}">
                        <a href="${pageContext.request.contextPath}/member/notifications" class="notif-badge" title="Notifications">
                            <span style="font-size: 1.25rem;">🔔</span>
                            <c:if test="${sessionScope.unreadNotificationsCount > 0}">
                                <span class="notif-count">${sessionScope.unreadNotificationsCount}</span>
                            </c:if>
                        </a>
                    </c:if>

                    <a href="${sessionScope.userRole eq 'MEMBER' ? pageContext.request.contextPath.concat('/member/profile') : '#'}" class="user-chip">
                        <span class="user-avatar">${sessionScope.currentUser.email.substring(0, 1).toUpperCase()}</span>
                        <span>${sessionScope.currentUser.email}</span>
                        <span class="badge ${sessionScope.userRole eq 'LIBRARIAN' ? 'badge-danger' : 'badge-info'}" style="margin-left: 4px;">
                            ${sessionScope.userRole}
                        </span>
                    </a>

                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm">Logout</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-sm">Sign In</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Register</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
