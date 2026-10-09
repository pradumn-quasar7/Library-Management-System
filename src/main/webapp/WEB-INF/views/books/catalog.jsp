<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Discover Books — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Library Book Discovery</h1>
        <p class="subtitle">Explore thousands of scholarly, technology, and classic titles</p>
    </div>
</div>

<!-- Search & Filter Bar -->
<div class="search-filter-bar">
    <form action="${pageContext.request.contextPath}/books/search" method="GET" class="search-form">
        <div class="search-input-wrapper">
            <input type="text" name="q" class="form-control" value="${query}" placeholder="Search by title, ISBN, author, or keyword...">
        </div>
        <div class="filter-select-wrapper">
            <select name="genre" class="form-select">
                <option value="">All Genres</option>
                <c:forEach var="g" items="${genres}">
                    <option value="${g.id}" ${selectedGenreId eq g.id ? 'selected' : ''}>${g.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="filter-select-wrapper" style="flex: 0 0 auto;">
            <label style="display: flex; align-items: center; gap: 0.4rem; font-size: 0.88rem; font-weight: 600; cursor: pointer;">
                <input type="checkbox" name="available" value="true" ${availableOnly ? 'checked' : ''}>
                Available Only
            </label>
        </div>
        <div class="filter-select-wrapper">
            <select name="sort" class="form-select">
                <option value="title_asc" ${sortBy eq 'title_asc' ? 'selected' : ''}>Sort: Title (A-Z)</option>
                <option value="year_desc" ${sortBy eq 'year_desc' ? 'selected' : ''}>Sort: Year (Newest)</option>
                <option value="year_asc" ${sortBy eq 'year_asc' ? 'selected' : ''}>Sort: Year (Oldest)</option>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">Filter</button>
        <a href="${pageContext.request.contextPath}/books/search" class="btn btn-secondary">Reset</a>
    </form>
</div>

<!-- Books Grid -->
<div class="books-grid">
    <c:choose>
        <c:when test="${not empty books}">
            <c:forEach var="b" items="${books}">
                <div class="book-card">
                    <div>
                        <div class="book-cover-placeholder">
                            <div class="book-cover-title">${b.title}</div>
                            <div class="book-cover-author">${b.authorsFormatted}</div>
                        </div>
                        <div class="badge badge-secondary" style="margin-bottom: 0.5rem;">${b.genreName}</div>
                        <h3 class="book-title">
                            <a href="${pageContext.request.contextPath}/books/view?id=${b.id}">${b.title}</a>
                        </h3>
                        <p class="book-author">By ${b.authorsFormatted}</p>
                        <p style="font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 0.75rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                            ${b.description}
                        </p>
                    </div>

                    <div>
                        <div class="book-meta">
                            <span>ISBN: <code>${b.isbn}</code></span>
                            <span class="badge ${b.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">
                                ${b.availableCopies} / ${b.totalCopies} Available
                            </span>
                        </div>

                        <div style="display: flex; gap: 0.5rem; margin-top: 1rem;">
                            <a href="${pageContext.request.contextPath}/books/view?id=${b.id}" class="btn btn-secondary btn-sm" style="flex: 1;">Details</a>
                            <c:choose>
                                <c:when test="${sessionScope.userRole eq 'MEMBER'}">
                                    <c:choose>
                                        <c:when test="${b.isAvailable()}">
                                            <form action="${pageContext.request.contextPath}/member/borrow" method="POST" style="flex: 1;">
                                                <input type="hidden" name="bookId" value="${b.id}"/>
                                                <button type="submit" class="btn btn-primary btn-sm" style="width: 100%;">Borrow</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="${pageContext.request.contextPath}/member/reserve" method="POST" style="flex: 1;">
                                                <input type="hidden" name="bookId" value="${b.id}"/>
                                                <button type="submit" class="btn btn-secondary btn-sm" style="width: 100%;">Reserve</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:when test="${empty sessionScope.currentUser}">
                                    <a href="${pageContext.request.contextPath}/login" class="btn btn-primary btn-sm" style="flex: 1;">Sign In to Borrow</a>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-state-icon">📚</div>
                <h3>No books found matching your query</h3>
                <p>Try searching for a different keyword or resetting your genre filter.</p>
                <a href="${pageContext.request.contextPath}/books/search" class="btn btn-secondary" style="margin-top: 1rem;">Clear Filters</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
