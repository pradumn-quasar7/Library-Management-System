<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Smart Recommendations — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Smart Recommendations For You</h1>
        <p class="subtitle">Personalized book suggestions computed from your genre and author reading history</p>
    </div>
</div>

<div class="books-grid">
    <c:choose>
        <c:when test="${not empty recommendations}">
            <c:forEach var="b" items="${recommendations}">
                <div class="book-card">
                    <div>
                        <div class="book-cover-placeholder">
                            <div class="book-cover-title">${b.title}</div>
                            <div class="book-cover-author">${b.authorsFormatted}</div>
                        </div>
                        <span class="badge badge-info" style="margin-bottom: 0.5rem;">Recommended Match</span>
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
                            <span>Genre: <strong>${b.genreName}</strong></span>
                            <span class="badge ${b.availableCopies > 0 ? 'badge-success' : 'badge-danger'}">
                                ${b.availableCopies} / ${b.totalCopies} Available
                            </span>
                        </div>

                        <div style="display: flex; gap: 0.5rem; margin-top: 1rem;">
                            <a href="${pageContext.request.contextPath}/books/view?id=${b.id}" class="btn btn-secondary btn-sm" style="flex: 1;">Details</a>
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
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div class="empty-state" style="grid-column: 1 / -1;">
                <div class="empty-state-icon">💡</div>
                <h3>Building your personalized profile</h3>
                <p>As you borrow more titles, our Java recommendation engine will suggest matching books here!</p>
                <a href="${pageContext.request.contextPath}/books/search" class="btn btn-primary" style="margin-top: 1rem;">Browse Books</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
