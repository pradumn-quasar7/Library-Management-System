<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Error — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card" style="text-align: center;">
        <div style="font-size: 4rem; margin-bottom: 1rem;">🛡️</div>
        <h1>Library Notice</h1>
        <p style="color: var(--text-secondary); margin: 1rem 0 1.5rem;">
            ${requestScope.errorMessage != null ? requestScope.errorMessage : "An unexpected condition occurred. Please verify your input and try again."}
        </p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Homepage</a>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
