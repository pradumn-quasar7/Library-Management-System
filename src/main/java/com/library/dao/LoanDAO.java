package com.library.dao;

import com.library.model.Loan;
import com.library.model.LoanStatus;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public interface LoanDAO {
    Loan findById(Long id);
    Loan findById(Connection conn, Long id);
    List<Loan> findActiveLoansByMemberId(Long memberId);
    List<Loan> findAllLoansByMemberId(Long memberId);
    List<Loan> findAll(String statusFilter);
    List<Loan> findOverdueLoans();
    List<Loan> findLoansDueWithinHours(int hours);
    Long create(Connection conn, Loan loan);
    boolean updateReturn(Connection conn, Long loanId, Timestamp returnTime);
    boolean updateDueDate(Connection conn, Long loanId, LocalDate newDueDate, int newRenewalCount);
    boolean updateStatus(Connection conn, Long loanId, LoanStatus status);
    int countActiveLoansByMemberId(Long memberId);
    int countLoansByStatus(LoanStatus status);
}
