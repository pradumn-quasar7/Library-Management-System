<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Books — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Library Book Catalog</h1>
        <p class="subtitle">Search, filter, edit, and manage physical inventory copies</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/librarian/books/create" class="btn btn-primary">➕ Add New Book</a>
    </div>
</div>

<!-- Search & Filter Bar -->
<div class="search-filter-bar">
    <form action="${pageContext.request.contextPath}/librarian/books" method="GET" class="search-form">
        <div class="search-input-wrapper">
            <input type="text" name="q" class="form-control" value="${query}" placeholder="Search by title, ISBN, author, publisher...">
        </div>
        <div class="filter-select-wrapper">
            <select name="genre" class="form-select">
                <option value="">All Genres</option>
                <c:forEach var="g" items="${genres}">
                    <option value="${g.id}" ${selectedGenreId eq g.id ? 'selected' : ''}>${g.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="filter-select-wrapper">
            <select name="sort" class="form-select">
                <option value="title_asc" ${sortBy eq 'title_asc' ? 'selected' : ''}>Title (A-Z)</option>
                <option value="title_desc" ${sortBy eq 'title_desc' ? 'selected' : ''}>Title (Z-A)</option>
                <option value="year_desc" ${sortBy eq 'year_desc' ? 'selected' : ''}>Year (Newest)</option>
                <option value="year_asc" ${sortBy eq 'year_asc' ? 'selected' : ''}>Year (Oldest)</option>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">Filter</button>
        <a href="${pageContext.request.contextPath}/librarian/books" class="btn btn-secondary">Reset</a>
    </form>
</div>

<div class="table-responsive">
    <table class="table">
        <thead>
            <tr>
                <th>Title &amp; Authors</th>
                <th>ISBN</th>
                <th>Genre</th>
                <th>Copies Available</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:choose>
                <c:when test="${not empty books}">
                    <c:forEach var="b" items="${books}">
                        <tr>
                            <td>
                                <strong>${b.title}</strong><br/>
                                <small style="color: var(--text-secondary);">${b.authorsFormatted}</small>
                            </td>
                            <td><code>${b.isbn}</code></td>
                            <td><span class="badge badge-secondary">${b.genreName}</span></td>
                            <td>
                                <span class="badge ${b.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">
                                    ${b.availableCopies} / ${b.totalCopies} Available
                                </span>
                            </td>
                            <td>
                                <span class="badge ${b.status eq 'ACTIVE' ? 'badge-success' : 'badge-danger'}">
                                    ${b.status}
                                </span>
                            </td>
                            <td>
                                <div style="display: flex; gap: 0.35rem;">
                                    <a href="${pageContext.request.contextPath}/librarian/books/edit?id=${b.id}" class="btn btn-secondary btn-sm">Edit / Copies</a>
                                    <form action="${pageContext.request.contextPath}/librarian/books/delete" method="POST" onsubmit="return confirm('Change status of this book?');">
                                        <input type="hidden" name="id" value="${b.id}"/>
                                        <input type="hidden" name="status" value="${b.status eq 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}"/>
                                        <button type="submit" class="btn ${b.status eq 'ACTIVE' ? 'btn-danger' : 'btn-success'} btn-sm">
                                            ${b.status eq 'ACTIVE' ? 'Deactivate' : 'Activate'}
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <tr>
                        <td colspan="6" class="empty-state">No books matched your criteria.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
        </tbody>
    </table>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
