package com.library.dao;

import com.library.model.Fine;
import com.library.model.FineStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

public interface FineDAO {
    Fine findById(Long id);
    List<Fine> findByMemberId(Long memberId);
    List<Fine> findAll(String statusFilter);
    Long create(Connection conn, Fine fine);
    boolean updateStatus(Long fineId, FineStatus status, Timestamp paidAt);
    BigDecimal getTotalUnpaidFines();
    BigDecimal getUnpaidFinesByMemberId(Long memberId);
}
