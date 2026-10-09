package com.library.dao.impl;

import com.library.dao.ReadingRoomDAO;
import com.library.exception.DatabaseException;
import com.library.model.BookingStatus;
import com.library.model.ReadingRoomBooking;
import com.library.model.ReadingRoomSeat;
import com.library.model.SeatStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReadingRoomDAOImpl implements ReadingRoomDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReadingRoomDAOImpl.class);

    private static final String BASE_BOOKING_QUERY =
            "SELECT b.id, b.member_id, b.seat_id, b.booking_date, b.start_time, b.end_time, b.status, b.created_at, " +
            "s.seat_number, m.full_name AS member_name, m.membership_id AS member_membership_id " +
            "FROM reading_room_bookings b " +
            "JOIN reading_room_seats s ON b.seat_id = s.id " +
            "JOIN members m ON b.member_id = m.id ";

    @Override
    public List<ReadingRoomSeat> findAllSeats() {
        String sql = "SELECT id, seat_number, status FROM reading_room_seats ORDER BY seat_number ASC";
        List<ReadingRoomSeat> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ReadingRoomSeat seat = new ReadingRoomSeat();
                seat.setId(rs.getLong("id"));
                seat.setSeatNumber(rs.getString("seat_number"));
                seat.setStatus(SeatStatus.fromString(rs.getString("status")));
                list.add(seat);
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error listing all reading room seats", e);
            throw new DatabaseException("Failed to list reading room seats", e);
        }
    }

    @Override
    public ReadingRoomSeat findSeatById(Long id) {
        String sql = "SELECT id, seat_number, status FROM reading_room_seats WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ReadingRoomSeat seat = new ReadingRoomSeat();
                    seat.setId(rs.getLong("id"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    seat.setStatus(SeatStatus.fromString(rs.getString("status")));
                    return seat;
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding seat by id: {}", id, e);
            throw new DatabaseException("Failed to find reading room seat", e);
        }
    }

    @Override
    public List<ReadingRoomBooking> findBookingsByDate(LocalDate date) {
        String sql = BASE_BOOKING_QUERY + "WHERE b.booking_date = ? AND b.status = 'BOOKED' ORDER BY b.start_time ASC";
        List<ReadingRoomBooking> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding bookings for date: {}", date, e);
            throw new DatabaseException("Failed to find bookings by date", e);
        }
    }

    @Override
    public List<ReadingRoomBooking> findBookingsByMemberId(Long memberId) {
        String sql = BASE_BOOKING_QUERY + "WHERE b.member_id = ? ORDER BY b.booking_date DESC, b.start_time DESC";
        List<ReadingRoomBooking> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBookingRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding bookings for member: {}", memberId, e);
            throw new DatabaseException("Failed to find member bookings", e);
        }
    }

    @Override
    public boolean isSeatAvailable(Connection conn, Long seatId, LocalDate date, Time startTime, Time endTime) {
        // Overlap condition: start_time < requested_end AND end_time > requested_start
        String sql = "SELECT COUNT(*) FROM reading_room_bookings " +
                "WHERE seat_id = ? AND booking_date = ? AND status = 'BOOKED' " +
                "AND start_time < ? AND end_time > ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, endTime);
            ps.setTime(4, startTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
            return true;
        } catch (SQLException e) {
            logger.error("Error checking seat availability for seat {} on {}", seatId, date, e);
            throw new DatabaseException("Failed to check seat availability", e);
        }
    }

    @Override
    public Long createBooking(Connection conn, ReadingRoomBooking booking) {
        String sql = "INSERT INTO reading_room_bookings (member_id, seat_id, booking_date, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getMemberId());
            ps.setLong(2, booking.getSeatId());
            ps.setDate(3, Date.valueOf(booking.getBookingDate()));
            ps.setTime(4, booking.getStartTime());
            ps.setTime(5, booking.getEndTime());
            ps.setString(6, booking.getStatus() != null ? booking.getStatus().name() : BookingStatus.BOOKED.name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        booking.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to insert booking, no ID returned.");
        } catch (SQLException e) {
            logger.error("Error creating reading room booking in transaction", e);
            throw new DatabaseException("Failed to record booking", e);
        }
    }

    @Override
    public boolean cancelBooking(Long bookingId, Long memberId) {
        String sql = "UPDATE reading_room_bookings SET status = 'CANCELLED' WHERE id = ? AND member_id = ? AND status = 'BOOKED'";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setLong(2, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error cancelling booking {}", bookingId, e);
            throw new DatabaseException("Failed to cancel booking", e);
        }
    }

    @Override
    public int countBookingsByMemberForDate(Long memberId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM reading_room_bookings WHERE member_id = ? AND booking_date = ? AND status = 'BOOKED'";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting member daily bookings", e);
            throw new DatabaseException("Failed to count daily bookings", e);
        }
    }

    @Override
    public int countTodayBookings() {
        String sql = "SELECT COUNT(*) FROM reading_room_bookings WHERE booking_date = CURRENT_DATE AND status = 'BOOKED'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting today bookings", e);
            throw new DatabaseException("Failed to count today bookings", e);
        }
    }

    private ReadingRoomBooking mapBookingRow(ResultSet rs) throws SQLException {
        ReadingRoomBooking b = new ReadingRoomBooking();
        b.setId(rs.getLong("id"));
        b.setMemberId(rs.getLong("member_id"));
        b.setSeatId(rs.getLong("seat_id"));
        Date d = rs.getDate("booking_date");
        if (d != null) {
            b.setBookingDate(d.toLocalDate());
        }
        b.setStartTime(rs.getTime("start_time"));
        b.setEndTime(rs.getTime("end_time"));
        b.setStatus(BookingStatus.fromString(rs.getString("status")));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        b.setSeatNumber(rs.getString("seat_number"));
        b.setMemberName(rs.getString("member_name"));
        b.setMemberMembershipId(rs.getString("member_membership_id"));
        return b;
    }
}
