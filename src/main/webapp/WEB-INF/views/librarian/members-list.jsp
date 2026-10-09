<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Members — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Registered Library Members</h1>
        <p class="subtitle">Search members, monitor circulation status, and manage access</p>
    </div>
</div>

<div class="search-filter-bar">
    <form action="${pageContext.request.contextPath}/librarian/members" method="GET" class="search-form">
        <div class="search-input-wrapper">
            <input type="text" name="q" class="form-control" value="${query}" placeholder="Search by name, Membership ID, or email...">
        </div>
        <button type="submit" class="btn btn-primary">Search</button>
        <a href="${pageContext.request.contextPath}/librarian/members" class="btn btn-secondary">Reset</a>
    </form>
</div>

<div class="table-responsive">
    <table class="table">
        <thead>
            <tr>
                <th>Membership ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Joined Date</th>
                <th>Account Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty members}">
                    <c:forEach var="m" items="${members}">
                        <tr>
                            <td><code>${m.membershipId}</code></td>
                            <td><strong>${m.fullName}</strong></td>
                            <td>${m.email}</td>
                            <td>${m.phone != null ? m.phone : "—"}</td>
                            <td>${m.joinedAt}</td>
                            <td>
                                <span class="badge ${m.status eq 'ACTIVE' ? 'badge-success' : (m.status eq 'BLOCKED' ? 'badge-danger' : 'badge-warning')}">
                                    ${m.status}
                                </span>
                            </td>
                            <td>
                                <div style="display: flex; gap: 0.35rem;">
                                    <a href="${pageContext.request.contextPath}/librarian/members?id=${m.id}" class="btn btn-secondary btn-sm">View Profile &amp; Loans</a>
                                    <form action="${pageContext.request.contextPath}/librarian/members/status" method="POST" style="display: inline;">
                                        <input type="hidden" name="id" value="${m.id}"/>
                                        <select name="status" onchange="this.form.submit()" class="form-select" style="padding: 2px 4px; font-size: 0.75rem; width: auto; display: inline-block;">
                                            <option value="ACTIVE" ${m.status eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                            <option value="BLOCKED" ${m.status eq 'BLOCKED' ? 'selected' : ''}>BLOCKED</option>
                                            <option value="INACTIVE" ${m.status eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                        </select>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr><td colspan="7" class="empty-state">No members found.</td></tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
