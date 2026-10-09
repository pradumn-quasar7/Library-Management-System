package com.library.dao.impl;

import com.library.dao.ReservationDAO;
import com.library.exception.DatabaseException;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAOImpl implements ReservationDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReservationDAOImpl.class);

    private static final String BASE_QUERY =
            "SELECT r.id, r.book_id, r.member_id, r.reserved_at, r.queue_position, r.status, r.expires_at, " +
            "b.title AS book_title, b.isbn AS book_isbn, " +
            "m.full_name AS member_name, u.email AS member_email " +
            "FROM reservations r " +
            "JOIN books b ON r.book_id = b.id " +
            "JOIN members m ON r.member_id = m.id " +
            "JOIN users u ON m.user_id = u.id ";

    @Override
    public Reservation findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Error finding reservation by id: {}", id, e);
            throw new DatabaseException("Failed to find reservation", e);
        }
    }

    @Override
    public Reservation findById(Connection conn, Long id) {
        String sql = BASE_QUERY + "WHERE r.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding reservation by id in conn: {}", id, e);
            throw new DatabaseException("Failed to find reservation record", e);
        }
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        String sql = BASE_QUERY + "WHERE r.member_id = ? ORDER BY r.reserved_at DESC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding reservations for member: {}", memberId, e);
            throw new DatabaseException("Failed to find reservations", e);
        }
    }

    @Override
    public List<Reservation> findActiveByBookId(Long bookId) {
        String sql = BASE_QUERY + "WHERE r.book_id = ? AND r.status IN ('WAITING', 'READY') ORDER BY r.queue_position ASC";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding active reservations for book: {}", bookId, e);
            throw new DatabaseException("Failed to find reservations for book", e);
        }
    }

    @Override
    public Reservation findEarliestWaitingReservation(Connection conn, Long bookId) {
        String sql = BASE_QUERY + "WHERE r.book_id = ? AND r.status = 'WAITING' ORDER BY r.queue_position ASC, r.reserved_at ASC LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding earliest waiting reservation for book: {}", bookId, e);
            throw new DatabaseException("Failed to fetch earliest waiting reservation", e);
        }
    }

    @Override
    public Long create(Connection conn, Reservation reservation) {
        String sql = "INSERT INTO reservations (book_id, member_id, reserved_at, queue_position, status, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, reservation.getBookId());
            ps.setLong(2, reservation.getMemberId());
            ps.setTimestamp(3, reservation.getReservedAt() != null ? reservation.getReservedAt() : new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, reservation.getQueuePosition());
            ps.setString(5, reservation.getStatus() != null ? reservation.getStatus().name() : ReservationStatus.WAITING.name());
            ps.setTimestamp(6, reservation.getExpiresAt());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        reservation.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to insert reservation, no ID returned.");
        } catch (SQLException e) {
            logger.error("Error creating reservation", e);
            throw new DatabaseException("Failed to create reservation", e);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Long reservationId, ReservationStatus status, Timestamp expiresAt) {
        String sql = "UPDATE reservations SET status = ?, expires_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, expiresAt);
            ps.setLong(3, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for reservation {}", reservationId, e);
            throw new DatabaseException("Failed to update reservation status", e);
        }
    }

    @Override
    public boolean cancel(Long reservationId, Long memberId) {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE id = ? AND member_id = ? AND status IN ('WAITING', 'READY')";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            ps.setLong(2, memberId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error cancelling reservation {}", reservationId, e);
            throw new DatabaseException("Failed to cancel reservation", e);
        }
    }

    @Override
    public boolean isAlreadyReservedByMember(Long bookId, Long memberId) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE book_id = ? AND member_id = ? AND status IN ('WAITING', 'READY')";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            ps.setLong(2, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error checking existing reservation", e);
            throw new DatabaseException("Failed to check existing reservation", e);
        }
    }

    @Override
    public int getNextQueuePosition(Connection conn, Long bookId) {
        String sql = "SELECT COALESCE(MAX(queue_position), 0) + 1 FROM reservations WHERE book_id = ? AND status IN ('WAITING', 'READY')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 1;
        } catch (SQLException e) {
            logger.error("Error determining next queue position for book {}", bookId, e);
            throw new DatabaseException("Failed to calculate queue position", e);
        }
    }

    @Override
    public int countWaitingReservations() {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status = 'WAITING'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting waiting reservations", e);
            throw new DatabaseException("Failed to count reservations", e);
        }
    }

    @Override
    public List<Reservation> findExpiredReadyReservations() {
        String sql = BASE_QUERY + "WHERE r.status = 'READY' AND r.expires_at < CURRENT_TIMESTAMP";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding expired ready reservations", e);
            throw new DatabaseException("Failed to find expired reservations", e);
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getLong("id"));
        res.setBookId(rs.getLong("book_id"));
        res.setMemberId(rs.getLong("member_id"));
        res.setReservedAt(rs.getTimestamp("reserved_at"));
        res.setQueuePosition(rs.getInt("queue_position"));
        res.setStatus(ReservationStatus.fromString(rs.getString("status")));
        res.setExpiresAt(rs.getTimestamp("expires_at"));

        res.setBookTitle(rs.getString("book_title"));
        res.setBookIsbn(rs.getString("book_isbn"));
        res.setMemberName(rs.getString("member_name"));
        res.setMemberEmail(rs.getString("member_email"));
        return res;
    }
}
