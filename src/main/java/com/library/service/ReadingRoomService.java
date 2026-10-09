package com.library.service;

import com.library.dao.AuditLogDAO;
import com.library.dao.MemberDAO;
import com.library.dao.NotificationDAO;
import com.library.dao.ReadingRoomDAO;
import com.library.dao.impl.AuditLogDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.NotificationDAOImpl;
import com.library.dao.impl.ReadingRoomDAOImpl;
import com.library.exception.ConflictException;
import com.library.exception.DatabaseException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.*;
import com.library.util.ConnectionManager;
import com.library.validation.LibraryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public class ReadingRoomService {
    private static final Logger logger = LoggerFactory.getLogger(ReadingRoomService.class);

    private final ReadingRoomDAO readingRoomDAO;
    private final MemberDAO memberDAO;
    private final NotificationDAO notificationDAO;
    private final AuditLogDAO auditLogDAO;

    public ReadingRoomService() {
        this.readingRoomDAO = new ReadingRoomDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public ReadingRoomService(ReadingRoomDAO readingRoomDAO, MemberDAO memberDAO, NotificationDAO notificationDAO, AuditLogDAO auditLogDAO) {
        this.readingRoomDAO = readingRoomDAO;
        this.memberDAO = memberDAO;
        this.notificationDAO = notificationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<ReadingRoomSeat> getAllSeats() {
        return readingRoomDAO.findAllSeats();
    }

    public List<ReadingRoomBooking> getMemberBookings(Long memberId) {
        return readingRoomDAO.findBookingsByMemberId(memberId);
    }

    public List<ReadingRoomBooking> getBookingsByDate(LocalDate date) {
        return readingRoomDAO.findBookingsByDate(date);
    }

    public ReadingRoomBooking bookSeat(Long memberId, Long seatId, LocalDate date, Time startTime, Time endTime) {
        Member member = memberDAO.findById(memberId);
        if (member == null || !member.isActive()) {
            throw new ConflictException("Your membership must be active to book reading room seats.");
        }

        ReadingRoomSeat seat = readingRoomDAO.findSeatById(seatId);
        if (seat == null) {
            throw new ResourceNotFoundException("Reading room seat not found.");
        }

        if (SeatStatus.MAINTENANCE.equals(seat.getStatus())) {
            throw new ConflictException("Seat " + seat.getSeatNumber() + " is currently under maintenance.");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot book seats for past dates.");
        }

        if (!startTime.before(endTime)) {
            throw new ValidationException("Start time must be strictly before end time.");
        }

        // Check daily booking limit
        int dailyBookings = readingRoomDAO.countBookingsByMemberForDate(memberId, date);
        if (dailyBookings >= LibraryPolicy.MAX_DAILY_READING_ROOM_BOOKINGS) {
            throw new ConflictException("You have reached the maximum daily booking limit (" +
                    LibraryPolicy.MAX_DAILY_READING_ROOM_BOOKINGS + " slots per day).");
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // Atomic conflict check with row lock
            boolean available = readingRoomDAO.isSeatAvailable(conn, seatId, date, startTime, endTime);
            if (!available) {
                throw new ConflictException("Seat " + seat.getSeatNumber() + " is already booked during this time interval.");
            }

            ReadingRoomBooking booking = new ReadingRoomBooking();
            booking.setMemberId(memberId);
            booking.setSeatId(seatId);
            booking.setBookingDate(date);
            booking.setStartTime(startTime);
            booking.setEndTime(endTime);
            booking.setStatus(BookingStatus.BOOKED);

            Long bookingId = readingRoomDAO.createBooking(conn, booking);
            booking.setId(bookingId);

            notificationDAO.create(conn, new Notification(
                    memberId,
                    "Reading Room Seat Reserved",
                    "Seat " + seat.getSeatNumber() + " is booked for " + date + " from " + startTime + " to " + endTime + ".",
                    NotificationType.GENERAL
            ));

            auditLogDAO.create(conn, new AuditLog(
                    member.getUserId(),
                    "SEAT_BOOKED",
                    "READING_ROOM_BOOKING",
                    bookingId,
                    "Booked seat " + seat.getSeatNumber() + " on " + date + " (" + startTime + " - " + endTime + ")"
            ));

            conn.commit();
            logger.info("Seat {} successfully booked on {} by member {}", seat.getSeatNumber(), date, memberId);
            return booking;
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Booking transaction failed for seat {}", seatId, e);
            throw new DatabaseException("Failed to reserve seat: " + e.getMessage(), e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public void cancelBooking(Long bookingId, Long memberId) {
        boolean cancelled = readingRoomDAO.cancelBooking(bookingId, memberId);
        if (!cancelled) {
            throw new ConflictException("Unable to cancel booking. It may already be cancelled or belongs to another user.");
        }

        Member member = memberDAO.findById(memberId);
        auditLogDAO.create(new AuditLog(member.getUserId(), "BOOKING_CANCELLED", "READING_ROOM_BOOKING", bookingId, "Cancelled reading room booking #" + bookingId));
        logger.info("Booking {} cancelled by member {}", bookingId, memberId);
    }
}
