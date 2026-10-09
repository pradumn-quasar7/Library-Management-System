package com.library.dao.impl;

import com.library.dao.MemberDAO;
import com.library.exception.DatabaseException;
import com.library.model.Member;
import com.library.model.UserStatus;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAOImpl implements MemberDAO {
    private static final Logger logger = LoggerFactory.getLogger(MemberDAOImpl.class);

    private static final String BASE_QUERY =
            "SELECT m.id, m.user_id, m.membership_id, m.full_name, m.phone, m.address, m.joined_at, " +
            "u.email, u.status AS user_status " +
            "FROM members m " +
            "JOIN users u ON m.user_id = u.id ";

    @Override
    public Member findById(Long id) {
        String sql = BASE_QUERY + "WHERE m.id = ?";
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
            logger.error("Error finding member by id: {}", id, e);
            throw new DatabaseException("Failed to find member by ID", e);
        }
    }

    @Override
    public Member findByUserId(Long userId) {
        String sql = BASE_QUERY + "WHERE m.user_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding member by user id: {}", userId, e);
            throw new DatabaseException("Failed to find member by user ID", e);
        }
    }

    @Override
    public Member findByMembershipId(String membershipId) {
        String sql = BASE_QUERY + "WHERE LOWER(m.membership_id) = LOWER(?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, membershipId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error finding member by membership id: {}", membershipId, e);
            throw new DatabaseException("Failed to find member by membership ID", e);
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = BASE_QUERY + "ORDER BY m.id DESC";
        List<Member> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error retrieving all members", e);
            throw new DatabaseException("Failed to retrieve members list", e);
        }
    }

    @Override
    public List<Member> search(String query) {
        String sql = BASE_QUERY +
                "WHERE LOWER(m.full_name) LIKE ? OR LOWER(m.membership_id) LIKE ? OR LOWER(u.email) LIKE ? " +
                "ORDER BY m.full_name ASC";
        List<Member> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String wildcard = "%" + (query != null ? query.trim().toLowerCase() : "") + "%";
            ps.setString(1, wildcard);
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error searching members with query: {}", query, e);
            throw new DatabaseException("Failed to search members", e);
        }
    }

    @Override
    public Long create(Member member) {
        try (Connection conn = ConnectionManager.getConnection()) {
            return create(conn, member);
        } catch (SQLException e) {
            logger.error("Error creating member", e);
            throw new DatabaseException("Failed to create member", e);
        }
    }

    @Override
    public Long create(Connection conn, Member member) {
        String sql = "INSERT INTO members (user_id, membership_id, full_name, phone, address, joined_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, member.getUserId());
            ps.setString(2, member.getMembershipId());
            ps.setString(3, member.getFullName());
            ps.setString(4, member.getPhone());
            ps.setString(5, member.getAddress());
            ps.setDate(6, Date.valueOf(member.getJoinedAt()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        long id = keys.getLong(1);
                        member.setId(id);
                        return id;
                    }
                }
            }
            throw new DatabaseException("Creating member failed, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error inserting member in transaction", e);
            throw new DatabaseException("Failed to insert member", e);
        }
    }

    @Override
    public boolean update(Member member) {
        String sql = "UPDATE members SET full_name = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getFullName());
            ps.setString(2, member.getPhone());
            ps.setString(3, member.getAddress());
            ps.setLong(4, member.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating member: {}", member.getId(), e);
            throw new DatabaseException("Failed to update member profile", e);
        }
    }

    @Override
    public int countActiveMembers() {
        String sql = "SELECT COUNT(*) FROM members m JOIN users u ON m.user_id = u.id WHERE u.status = 'ACTIVE'";
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting active members", e);
            throw new DatabaseException("Failed to count members", e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getLong("id"));
        member.setUserId(rs.getLong("user_id"));
        member.setMembershipId(rs.getString("membership_id"));
        member.setFullName(rs.getString("full_name"));
        member.setPhone(rs.getString("phone"));
        member.setAddress(rs.getString("address"));
        Date joined = rs.getDate("joined_at");
        if (joined != null) {
            member.setJoinedAt(joined.toLocalDate());
        }
        member.setEmail(rs.getString("email"));
        member.setStatus(UserStatus.fromString(rs.getString("user_status")));
        return member;
    }
}
