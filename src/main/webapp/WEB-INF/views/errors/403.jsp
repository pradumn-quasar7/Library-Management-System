<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="403 - Access Denied — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card" style="text-align: center;">
        <div style="font-size: 4rem; margin-bottom: 1rem;">🚫</div>
        <h1>403 - Access Forbidden</h1>
        <p style="color: var(--text-secondary); margin: 1rem 0 2rem;">
            You do not have the required permissions or role to view this restricted page.
        </p>
        <div style="display: flex; gap: 0.5rem; justify-content: center;">
            <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Homepage</a>
            <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Switch Account</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
