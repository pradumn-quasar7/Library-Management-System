<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="500 - System Error — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card" style="text-align: center;">
        <div style="font-size: 4rem; margin-bottom: 1rem;">⚙️</div>
        <h1>Something went wrong</h1>
        <p style="color: var(--text-secondary); margin: 1rem 0 1.5rem;">
            An internal server error occurred while processing your library request.
            The issue has been logged for system administrator review.
        </p>
        <div style="padding: 0.75rem; background: #f8fafc; border-radius: 8px; font-size: 0.85rem; margin-bottom: 2rem;">
            Incident Reference: <code>LIB-ERR-${System.currentTimeMillis()}</code>
        </div>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Safety</a>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
