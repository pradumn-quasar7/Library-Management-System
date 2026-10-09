package com.library.dao.impl;

import com.library.dao.LoanDAO;
import com.library.exception.DatabaseException;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {
    private static final Logger logger = LoggerFactory.getLogger(LoanDAOImpl.class);

    private static final String BASE_QUERY =
            "SELECT l.id, l.copy_id, l.member_id, l.borrowed_at, l.due_date, l.returned_at, l.renewal_count, l.status, " +
            "b.title AS book_title, b.isbn AS book_isbn, c.accession_number, " +
            "m.full_name AS member_name, u.email AS member_email, " +
            "COALESCE(f.amount, 0.00) AS fine_amount " +
            "FROM loans l " +
            "JOIN book_copies c ON l.copy_id = c.id " +
            "JOIN books b ON c.book_id = b.id " +
            "JOIN members m ON l.member_id = m.id " +
            "JOIN users u ON m.user_id = u.id " +
            "LEFT JOIN fines f ON l.id = f.loan_id ";

    @Override
    public Loan findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Error finding loan by id: {}", id, e);
            throw new DatabaseException("Failed to find loan by ID", e);
        }
    }

    @Override
    public Loan findById(Connection conn, Long id) {
        String sql = BASE_QUERY + "WHERE l.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding loan by id in conn: {}", id, e);
            throw new DatabaseException("Failed to find loan record", e);
        }
    }

    @Override
    public List<Loan> findActiveLoansByMemberId(Long memberId) {
        String sql = BASE_QUERY + "WHERE l.member_id = ? AND l.status IN ('ACTIVE', 'OVERDUE') ORDER BY l.due_date ASC";
        List<Loan> list = new ArrayList<>();
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
            logger.error("Error finding active loans for member: {}", memberId, e);
            throw new DatabaseException("Failed to find active loans", e);
        }
    }

    @Override
    public List<Loan> findAllLoansByMemberId(Long memberId) {
        String sql = BASE_QUERY + "WHERE l.member_id = ? ORDER BY l.borrowed_at DESC";
        List<Loan> list = new ArrayList<>();
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
            logger.error("Error finding loans history for member: {}", memberId, e);
            throw new DatabaseException("Failed to find loans history", e);
        }
    }

    @Override
    public List<Loan> findAll(String statusFilter) {
        StringBuilder sb = new StringBuilder(BASE_QUERY);
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sb.append("WHERE l.status = ? ");
        }
        sb.append("ORDER BY l.borrowed_at DESC");

        List<Loan> list = new ArrayList<>();
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
            logger.error("Error finding all loans with filter: {}", statusFilter, e);
            throw new DatabaseException("Failed to list loans", e);
        }
    }

    @Override
    public List<Loan> findOverdueLoans() {
        String sql = BASE_QUERY + "WHERE l.status IN ('ACTIVE', 'OVERDUE') AND l.due_date < CURRENT_DATE ORDER BY l.due_date ASC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding overdue loans", e);
            throw new DatabaseException("Failed to list overdue loans", e);
        }
    }

    @Override
    public List<Loan> findLoansDueWithinHours(int hours) {
        String sql = BASE_QUERY + "WHERE l.status = 'ACTIVE' AND l.due_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? HOUR) ORDER BY l.due_date ASC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hours);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error finding loans due within {} hours", hours, e);
            throw new DatabaseException("Failed to find loans due soon", e);
        }
    }

    @Override
    public Long create(Connection conn, Loan loan) {
        String sql = "INSERT INTO loans (copy_id, member_id, borrowed_at, due_date, renewal_count, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, loan.getCopyId());
            ps.setLong(2, loan.getMemberId());
            ps.setTimestamp(3, loan.getBorrowedAt() != null ? loan.getBorrowedAt() : new Timestamp(System.currentTimeMillis()));
            ps.setDate(4, Date.valueOf(loan.getDueDate()));
            ps.setInt(5, loan.getRenewalCount());
            ps.setString(6, loan.getStatus() != null ? loan.getStatus().name() : LoanStatus.ACTIVE.name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        loan.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Failed to insert loan, no ID returned.");
        } catch (SQLException e) {
            logger.error("Error inserting loan in transaction", e);
            throw new DatabaseException("Failed to insert loan record", e);
        }
    }

    @Override
    public boolean updateReturn(Connection conn, Long loanId, Timestamp returnTime) {
        String sql = "UPDATE loans SET returned_at = ?, status = 'RETURNED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, returnTime != null ? returnTime : new Timestamp(System.currentTimeMillis()));
            ps.setLong(2, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating return status for loan {}", loanId, e);
            throw new DatabaseException("Failed to record return", e);
        }
    }

    @Override
    public boolean updateDueDate(Connection conn, Long loanId, LocalDate newDueDate, int newRenewalCount) {
        String sql = "UPDATE loans SET due_date = ?, renewal_count = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(newDueDate));
            ps.setInt(2, newRenewalCount);
            ps.setLong(3, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error extending due date for loan {}", loanId, e);
            throw new DatabaseException("Failed to renew loan", e);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Long loanId, LoanStatus status) {
        String sql = "UPDATE loans SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating loan status for id {}", loanId, e);
            throw new DatabaseException("Failed to update loan status", e);
        }
    }

    @Override
    public int countActiveLoansByMemberId(Long memberId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND status IN ('ACTIVE', 'OVERDUE')";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting active loans for member: {}", memberId, e);
            throw new DatabaseException("Failed to count active loans", e);
        }
    }

    @Override
    public int countLoansByStatus(LoanStatus status) {
        String sql = "SELECT COUNT(*) FROM loans WHERE status = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting loans by status {}", status, e);
            throw new DatabaseException("Failed to count loans", e);
        }
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getLong("id"));
        loan.setCopyId(rs.getLong("copy_id"));
        loan.setMemberId(rs.getLong("member_id"));
        loan.setBorrowedAt(rs.getTimestamp("borrowed_at"));
        Date due = rs.getDate("due_date");
        if (due != null) {
            loan.setDueDate(due.toLocalDate());
        }
        loan.setReturnedAt(rs.getTimestamp("returned_at"));
        loan.setRenewalCount(rs.getInt("renewal_count"));
        loan.setStatus(LoanStatus.fromString(rs.getString("status")));

        loan.setBookTitle(rs.getString("book_title"));
        loan.setBookIsbn(rs.getString("book_isbn"));
        loan.setAccessionNumber(rs.getString("accession_number"));
        loan.setMemberName(rs.getString("member_name"));
        loan.setMemberEmail(rs.getString("member_email"));
        loan.setFineAmount(rs.getBigDecimal("fine_amount"));
        return loan;
    }
}
