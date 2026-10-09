package com.library.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long copyId;
    private Long memberId;
    private Timestamp borrowedAt;
    private LocalDate dueDate;
    private Timestamp returnedAt;
    private int renewalCount;
    private LoanStatus status;

    // Transient fields for display & business calculations
    private String bookTitle;
    private String bookIsbn;
    private String accessionNumber;
    private String memberName;
    private String memberEmail;
    private BigDecimal fineAmount;

    public Loan() {
    }

    public Loan(Long id, Long copyId, Long memberId, Timestamp borrowedAt, LocalDate dueDate, LoanStatus status) {
        this.id = id;
        this.copyId = copyId;
        this.memberId = memberId;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCopyId() {
        return copyId;
    }

    public void setCopyId(Long copyId) {
        this.copyId = copyId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Timestamp getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(Timestamp borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Timestamp getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Timestamp returnedAt) {
        this.returnedAt = returnedAt;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(int renewalCount) {
        this.renewalCount = renewalCount;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public boolean isActive() {
        return LoanStatus.ACTIVE.equals(this.status) || LoanStatus.OVERDUE.equals(this.status);
    }

    public boolean isReturned() {
        return LoanStatus.RETURNED.equals(this.status);
    }

    public boolean isOverdue() {
        if (LoanStatus.OVERDUE.equals(this.status)) return true;
        if (isActive() && dueDate != null && LocalDate.now().isAfter(dueDate)) {
            return true;
        }
        return false;
    }

    public long getDaysOverdue() {
        if (dueDate == null) return 0;
        LocalDate effectiveEndDate = (returnedAt != null) ? returnedAt.toLocalDateTime().toLocalDate() : LocalDate.now();
        if (effectiveEndDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, effectiveEndDate);
        }
        return 0;
    }

    public long getDaysRemaining() {
        if (dueDate == null || !isActive()) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", copyId=" + copyId +
                ", memberId=" + memberId +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
