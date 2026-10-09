<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="404 - Page Not Found — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card" style="text-align: center;">
        <div style="font-size: 4rem; margin-bottom: 1rem;">🔍</div>
        <h1>404 - Page Not Found</h1>
        <p style="color: var(--text-secondary); margin: 1rem 0 2rem;">
            The catalog resource, book title, or page you are looking for does not exist or has been relocated.
        </p>
        <div style="display: flex; gap: 0.5rem; justify-content: center;">
            <a href="${pageContext.request.contextPath}/books/search" class="btn btn-primary">Browse Catalog</a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Homepage</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
