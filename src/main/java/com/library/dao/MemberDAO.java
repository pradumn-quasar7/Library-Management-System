package com.library.dao;

import com.library.model.Member;

import java.sql.Connection;
import java.util.List;

public interface MemberDAO {
    Member findById(Long id);
    Member findByUserId(Long userId);
    Member findByMembershipId(String membershipId);
    List<Member> findAll();
    List<Member> search(String query);
    Long create(Member member);
    Long create(Connection conn, Member member);
    boolean update(Member member);
    int countActiveMembers();
}
