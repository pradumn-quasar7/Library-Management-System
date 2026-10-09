<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty sessionScope.successMessage}">
    <div class="alert alert-success">
        <span style="font-size: 1.25rem;">✅</span>
        <div>${sessionScope.successMessage}</div>
    </div>
    <c:remove var="successMessage" scope="session"/>
</c:if>

<c:if test="${not empty sessionScope.errorMessage}">
    <div class="alert alert-danger">
        <span style="font-size: 1.25rem;">⚠️</span>
        <div>${sessionScope.errorMessage}</div>
    </div>
    <c:remove var="errorMessage" scope="session"/>
</c:if>

<c:if test="${not empty requestScope.errorMessage}">
    <div class="alert alert-danger">
        <span style="font-size: 1.25rem;">⚠️</span>
        <div>${requestScope.errorMessage}</div>
    </div>
</c:if>

<c:if test="${not empty requestScope.successMessage}">
    <div class="alert alert-success">
        <span style="font-size: 1.25rem;">✅</span>
        <div>${requestScope.successMessage}</div>
    </div>
</c:if>
