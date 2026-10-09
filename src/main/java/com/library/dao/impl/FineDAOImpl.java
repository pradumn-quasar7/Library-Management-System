package com.library.dao.impl;

import com.library.dao.FineDAO;
import com.library.exception.DatabaseException;
import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAOImpl implements FineDAO {
    private static final Logger logger = LoggerFactory.getLogger(FineDAOImpl.class);

    private static final String BASE_QUERY =
            "SELECT f.id, f.loan_id, f.member_id, f.amount, f.reason, f.status, f.paid_at, f.created_at, " +
            "m.full_name AS member_name, m.membership_id AS member_membership_id, " +
            "b.title AS book_title, c.accession_number " +
            "FROM fines f " +
            "JOIN members m ON f.member_id = m.id " +
            "JOIN loans l ON f.loan_id = l.id " +
            "JOIN book_copies c ON l.copy_id = c.id " +
            "JOIN books b ON c.book_id = b.id ";

    @Override
    public Fine findById(Long id) {
        String sql = BASE_QUERY + "WHERE f.id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding fine by id: {}", id, e);
            throw new DatabaseException("Failed to find fine", e);
        }
    }

    @Override
    public List<Fine> findByMemberId(Long memberId) {
        String sql = BASE_QUERY + "WHERE f.member_id = ? ORDER BY f.created_at DESC";
        List<Fine> list = new ArrayList<>();
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
            logger.error("Error finding fines for member: {}", memberId, e);
            throw new DatabaseException("Failed to find member fines", e);
        }
    }

    @Override
    public List<Fine> findAll(String statusFilter) {
        StringBuilder sb = new StringBuilder(BASE_QUERY);
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sb.append("WHERE f.status = ? ");
        }
        sb.append("ORDER BY f.created_at DESC");

        List<Fine> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
                ps.setString(1, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error listing fines with filter: {}", statusFilter, e);
            throw new DatabaseException("Failed to list fines", e);
        }
    }

    @Override
    public Long create(Connection conn, Fine fine) {
        String sql = "INSERT INTO fines (loan_id, member_id, amount, reason, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, fine.getLoanId());
            ps.setLong(2, fine.getMemberId());
            ps.setBigDecimal(3, fine.getAmount());
            ps.setString(4, fine.getReason());
            ps.setString(5, fine.getStatus() != null ? fine.getStatus().name() : FineStatus.UNPAID.name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        fine.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to insert fine, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error inserting fine in transaction", e);
            throw new DatabaseException("Failed to record fine", e);
        }
    }

    @Override
    public boolean updateStatus(Long fineId, FineStatus status, Timestamp paidAt) {
        String sql = "UPDATE fines SET status = ?, paid_at = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, paidAt);
            ps.setLong(3, fineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating fine status: {}", fineId, e);
            throw new DatabaseException("Failed to update fine status", e);
        }
    }

    @Override
    public BigDecimal getTotalUnpaidFines() {
        String sql = "SELECT COALESCE(SUM(amount), 0.00) FROM fines WHERE status = 'UNPAID'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            logger.error("Error getting total unpaid fines", e);
            throw new DatabaseException("Failed to get total unpaid fines", e);
        }
    }

    @Override
    public BigDecimal getUnpaidFinesByMemberId(Long memberId) {
        String sql = "SELECT COALESCE(SUM(amount), 0.00) FROM fines WHERE member_id = ? AND status = 'UNPAID'";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            logger.error("Error getting unpaid fines for member: {}", memberId, e);
            throw new DatabaseException("Failed to get member unpaid fines", e);
        }
    }

    private Fine mapRow(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setId(rs.getLong("id"));
        fine.setLoanId(rs.getLong("loan_id"));
        fine.setMemberId(rs.getLong("member_id"));
        fine.setAmount(rs.getBigDecimal("amount"));
        fine.setReason(rs.getString("reason"));
        fine.setStatus(FineStatus.fromString(rs.getString("status")));
        fine.setPaidAt(rs.getTimestamp("paid_at"));
        fine.setCreatedAt(rs.getTimestamp("created_at"));

        fine.setMemberName(rs.getString("member_name"));
        fine.setMemberMembershipId(rs.getString("member_membership_id"));
        fine.setBookTitle(rs.getString("book_title"));
        fine.setAccessionNumber(rs.getString("accession_number"));
        return fine;
    }
}
