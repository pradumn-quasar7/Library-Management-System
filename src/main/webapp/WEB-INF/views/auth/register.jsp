<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register Library Membership — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card" style="max-width: 520px;">
        <div class="auth-header">
            <span style="font-size: 2.5rem;">💳</span>
            <h1>Join Athena Library</h1>
            <p class="subtitle">Create your digital membership account</p>
        </div>

        <form action="${pageContext.request.contextPath}/register" method="POST">
            <div class="form-group">
                <label class="form-label" for="fullName">Full Name *</label>
                <input type="text" id="fullName" name="fullName" class="form-control"
                       value="${requestScope.fullName}" placeholder="e.g. Jane Doe" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address *</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="${requestScope.email}" placeholder="jane@example.com" required>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="password">Password *</label>
                    <input type="password" id="password" name="password" class="form-control"
                           placeholder="At least 6 characters" required>
                </div>
                <div class="form-group">
                    <label class="form-label" for="phone">Phone Number</label>
                    <input type="text" id="phone" name="phone" class="form-control"
                           value="${requestScope.phone}" placeholder="+1-555-0199">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="address">Mailing Address</label>
                <input type="text" id="address" name="address" class="form-control"
                       value="${requestScope.address}" placeholder="Street, City, Zip">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Register Membership</button>
        </form>

        <div class="auth-footer">
            Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
