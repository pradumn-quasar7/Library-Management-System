<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reading Room Seat Reservation — Athena Library" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>Quiet Study &amp; Reading Room</h1>
        <p class="subtitle">Reserve a dedicated study carrel with high-speed Wi-Fi and power outlets</p>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Booking Form -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Book a Study Carrel</div>
        </div>

        <form action="${pageContext.request.contextPath}/member/reading-room" method="POST">
            <div class="form-group">
                <label class="form-label" for="seatId">Select Study Seat *</label>
                <select id="seatId" name="seatId" class="form-select" required>
                    <option value="">Choose Available Carrel</option>
                    <c:forEach var="s" items="${seats}">
                        <c:if test="${s.status eq 'AVAILABLE'}">
                            <option value="${s.id}">${s.seatNumber} — Study Carrel</option>
                        </c:if>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="date">Reservation Date *</label>
                <input type="date" id="date" name="date" class="form-control" value="${today}" min="${today}" required>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="startTime">Start Time *</label>
                    <select id="startTime" name="startTime" class="form-select" required>
                        <option value="09:00">09:00 AM</option>
                        <option value="10:00">10:00 AM</option>
                        <option value="11:00">11:00 AM</option>
                        <option value="12:00">12:00 PM</option>
                        <option value="13:00">01:00 PM</option>
                        <option value="14:00">02:00 PM</option>
                        <option value="15:00">03:00 PM</option>
                        <option value="16:00">04:00 PM</option>
                        <option value="17:00">05:00 PM</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label" for="endTime">End Time *</label>
                    <select id="endTime" name="endTime" class="form-select" required>
                        <option value="10:00">10:00 AM</option>
                        <option value="11:00">11:00 AM</option>
                        <option value="12:00">12:00 PM</option>
                        <option value="13:00">01:00 PM</option>
                        <option value="14:00">02:00 PM</option>
                        <option value="15:00">03:00 PM</option>
                        <option value="16:00">04:00 PM</option>
                        <option value="17:00">05:00 PM</option>
                        <option value="18:00" selected>06:00 PM</option>
                    </select>
                </div>
            </div>

            <div class="form-hint" style="margin-bottom: 1.25rem;">
                Maximum 2 slot reservations per day per member. Atomic overlap validation enforced.
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">Confirm Seat Reservation</button>
        </form>
    </div>

    <!-- My Bookings -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">My Reserved Slots (${myBookings.size()})</div>
        </div>

        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr>
                        <th>Seat</th>
                        <th>Date &amp; Time</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty myBookings}">
                            <c:forEach var="b" items="${myBookings}">
                                <tr>
                                    <td><strong>${b.seatNumber}</strong></td>
                                    <td>
                                        ${b.bookingDate}<br/>
                                        <small style="color: var(--text-muted);"><code>${b.startTime} - ${b.endTime}</code></small>
                                    </td>
                                    <td>
                                        <span class="badge ${b.status eq 'BOOKED' ? 'badge-success' : 'badge-secondary'}">
                                            ${b.status}
                                        </span>
                                    </td>
                                    <td>
                                        <c:if test="${b.status eq 'BOOKED'}">
                                            <form action="${pageContext.request.contextPath}/member/reading-room" method="POST" onsubmit="return confirm('Cancel this seat reservation?');">
                                                <input type="hidden" name="action" value="cancel"/>
                                                <input type="hidden" name="bookingId" value="${b.id}"/>
                                                <button type="submit" class="btn btn-danger btn-sm">Cancel</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="4" class="empty-state">No reading room bookings yet.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
