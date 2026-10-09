<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="400 - Invalid Request — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card" style="text-align: center;">
        <div style="font-size: 4rem; margin-bottom: 1rem;">⚠️</div>
        <h1>400 - Invalid Request</h1>
        <p style="color: var(--text-secondary); margin: 1rem 0 2rem;">
            The request could not be understood or was missing required parameters.
        </p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Return to Homepage</a>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
