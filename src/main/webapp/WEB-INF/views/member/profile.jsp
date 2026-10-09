<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Profile — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>My Account Profile</h1>
        <p class="subtitle">Manage your contact details and security credentials</p>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Contact Info -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Personal Information</div>
            <span class="badge badge-success">${member.status}</span>
        </div>

        <form action="${pageContext.request.contextPath}/member/profile" method="POST">
            <div class="form-group">
                <label class="form-label" for="membershipId">Membership ID</label>
                <input type="text" id="membershipId" class="form-control" value="${member.membershipId}" readonly style="background: #f1f5f9;">
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Email Address</label>
                <input type="text" id="email" class="form-control" value="${member.email}" readonly style="background: #f1f5f9;">
            </div>

            <div class="form-group">
                <label class="form-label" for="fullName">Full Name *</label>
                <input type="text" id="fullName" name="fullName" class="form-control" value="${member.fullName}" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="phone">Phone Number</label>
                <input type="text" id="phone" name="phone" class="form-control" value="${member.phone}">
            </div>

            <div class="form-group">
                <label class="form-label" for="address">Mailing Address</label>
                <input type="text" id="address" name="address" class="form-control" value="${member.address}">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">Update Profile Details</button>
        </form>
    </div>

    <!-- Security Credentials -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Change Password</div>
        </div>

        <form action="${pageContext.request.contextPath}/member/profile" method="POST">
            <input type="hidden" name="action" value="changePassword"/>

            <div class="form-group">
                <label class="form-label" for="currentPassword">Current Password *</label>
                <input type="password" id="currentPassword" name="currentPassword" class="form-control" required placeholder="••••••••">
            </div>

            <div class="form-group">
                <label class="form-label" for="newPassword">New Password *</label>
                <input type="password" id="newPassword" name="newPassword" class="form-control" required placeholder="At least 6 characters">
            </div>

            <div class="form-group">
                <label class="form-label" for="confirmPassword">Confirm New Password *</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" required placeholder="••••••••">
            </div>

            <button type="submit" class="btn btn-secondary" style="width: 100%;">Update Password</button>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
