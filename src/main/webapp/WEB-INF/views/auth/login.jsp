<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign In — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-header">
            <span style="font-size: 2.5rem;">🔐</span>
            <h1>Welcome Back</h1>
            <p class="subtitle">Enter your credentials to access your library portal</p>
        </div>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="${param.email != null ? param.email : (requestScope.email != null ? requestScope.email : '')}"
                       placeholder="name@library.local" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Sign In</button>
        </form>

        <div style="margin-top: 1.25rem; padding: 0.75rem; background: #f8fafc; border-radius: 8px; font-size: 0.82rem;">
            <strong>Demo Quick-Fills:</strong><br/>
            Librarian: <code>admin@library.local</code> / <code>Admin@123</code><br/>
            Member: <code>john.smith@library.local</code> / <code>Member@123</code>
        </div>

        <div class="auth-footer">
            Don't have a library card? <a href="${pageContext.request.contextPath}/register">Register here</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
