<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reading Room Occupancy — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Reading Room Schedules &amp; Occupancy</h1>
        <p class="subtitle">Real-time seat occupancy and booking schedule per day</p>
    </div>
</div>

<div class="search-filter-bar">
    <form action="${pageContext.request.contextPath}/librarian/reading-room" method="GET" class="search-form">
        <div class="search-input-wrapper" style="flex: 1;">
            <label class="form-label" style="margin-bottom: 0.2rem;">Select Inspection Date:</label>
            <input type="date" name="date" class="form-control" value="${selectedDate}">
        </div>
        <button type="submit" class="btn btn-primary" style="align-self: flex-end;">View Date</button>
    </form>
</div>

<div class="dashboard-grid">
    <!-- Seats Grid -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Study Carrels &amp; Seats Inventory (${seats.size()})</div>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); gap: 1rem;">
            <c:forEach var="s" items="${seats}">
                <div style="border: 1px solid var(--border-color); border-radius: 10px; padding: 1rem; text-align: center; background: #fafafa;">
                    <div style="font-size: 1.5rem; margin-bottom: 0.25rem;">🪑</div>
                    <strong>${s.seatNumber}</strong><br/>
                    <span class="badge ${s.status eq 'AVAILABLE' ? 'badge-success' : 'badge-danger'}" style="margin-top: 0.5rem;">
                        ${s.status}
                    </span>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Daily Bookings List -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Bookings for ${selectedDate} (${bookings.size()})</div>
        </div>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Seat</th>
                        <th>Member</th>
                        <th>Time Interval</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty bookings}">
                            <c:forEach var="b" items="${bookings}">
                                <tr>
                                    <td><strong>${b.seatNumber}</strong></td>
                                    <td>${b.memberName}</td>
                                    <td><code>${b.startTime} - ${b.endTime}</code></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="3" class="empty-state">No seat bookings on this date.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
