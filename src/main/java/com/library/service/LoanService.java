package com.library.service;

import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.exception.*;
import com.library.model.*;
import com.library.util.ConnectionManager;
import com.library.validation.LibraryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class LoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final LoanDAO loanDAO;
    private final BookDAO bookDAO;
    private final BookCopyDAO bookCopyDAO;
    private final MemberDAO memberDAO;
    private final FineDAO fineDAO;
    private final ReservationDAO reservationDAO;
    private final NotificationDAO notificationDAO;
    private final AuditLogDAO auditLogDAO;

    public LoanService() {
        this.loanDAO = new LoanDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.bookCopyDAO = new BookCopyDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.fineDAO = new FineDAOImpl();
        this.reservationDAO = new ReservationDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public LoanService(LoanDAO loanDAO, BookDAO bookDAO, BookCopyDAO bookCopyDAO, MemberDAO memberDAO,
                       FineDAO fineDAO, ReservationDAO reservationDAO, NotificationDAO notificationDAO, AuditLogDAO auditLogDAO) {
        this.loanDAO = loanDAO;
        this.bookDAO = bookDAO;
        this.bookCopyDAO = bookCopyDAO;
        this.memberDAO = memberDAO;
        this.fineDAO = fineDAO;
        this.reservationDAO = reservationDAO;
        this.notificationDAO = notificationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    /**
     * CRITICAL MULTI-STEP TRANSACTION: Borrow Book
     * Rules:
     * 1. Member must be ACTIVE
     * 2. Book must be ACTIVE
     * 3. Member active loans must not exceed LibraryPolicy.MAX_ACTIVE_LOANS (5)
     * 4. Member must not already hold an active loan of this book
     * 5. Atomically lock an available copy (FOR UPDATE)
     * 6. Insert loan record
     * 7. Update copy status to BORROWED
     * 8. Create notification
     * 9. Create audit log
     * 10. Commit transaction (or Rollback on failure)
     */
    public Loan borrowBook(Long memberId, Long bookId) {
        Member member = memberDAO.findById(memberId);
        if (member == null || !member.isActive()) {
            throw new MemberNotEligibleException("Member account is not active or eligible for borrowing.");
        }

        Book book = bookDAO.findById(bookId);
        if (book == null || !book.isActive()) {
            throw new ResourceNotFoundException("Book is not active or available in the catalog.");
        }

        // Check loan limits
        int activeLoansCount = loanDAO.countActiveLoansByMemberId(memberId);
        if (activeLoansCount >= LibraryPolicy.MAX_ACTIVE_LOANS) {
            throw new LoanLimitExceededException("Borrowing limit reached. Maximum allowed active loans is " + LibraryPolicy.MAX_ACTIVE_LOANS + ".");
        }

        // Check if member already has an active loan of this book
        List<Loan> activeLoans = loanDAO.findActiveLoansByMemberId(memberId);
        for (Loan l : activeLoans) {
            BookCopy c = bookCopyDAO.findById(l.getCopyId());
            if (c != null && c.getBookId().equals(bookId)) {
                throw new ConflictException("You already have an active loan for this title ('" + book.getTitle() + "').");
            }
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // Step 1: Atomically lock available copy
            BookCopy copy = bookCopyDAO.findFirstAvailableCopy(conn, bookId);
            if (copy == null) {
                throw new BookNotAvailableException("No copies of '" + book.getTitle() + "' are currently available for borrowing.");
            }

            // Step 2: Create Loan
            LocalDate dueDate = LocalDate.now().plusDays(LibraryPolicy.DEFAULT_LOAN_DAYS);
            Loan loan = new Loan();
            loan.setCopyId(copy.getId());
            loan.setMemberId(memberId);
            loan.setBorrowedAt(new Timestamp(System.currentTimeMillis()));
            loan.setDueDate(dueDate);
            loan.setRenewalCount(0);
            loan.setStatus(LoanStatus.ACTIVE);

            Long loanId = loanDAO.create(conn, loan);
            loan.setId(loanId);

            // Step 3: Update copy status to BORROWED
            bookCopyDAO.updateStatus(conn, copy.getId(), CopyStatus.BORROWED);

            // Step 4: Notification
            notificationDAO.create(conn, new Notification(
                    memberId,
                    "Book Borrowed: " + book.getTitle(),
                    "You borrowed copy " + copy.getAccessionNumber() + ". The due date is " + dueDate + ". Please return or renew on time.",
                    NotificationType.BORROW_CONFIRMATION
            ));

            // Step 5: Audit Log
            auditLogDAO.create(conn, new AuditLog(
                    member.getUserId(),
                    "BOOK_BORROWED",
                    "LOAN",
                    loanId,
                    "Member " + member.getFullName() + " borrowed copy " + copy.getAccessionNumber() + " of '" + book.getTitle() + "'"
            ));

            conn.commit();
            logger.info("Loan {} committed successfully: Member {} borrowed copy {}", loanId, memberId, copy.getId());
            return loanDAO.findById(loanId);
        } catch (SQLException | LibraryException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Transaction rolled back for borrowBook (member: {}, book: {})", memberId, bookId, e);
            if (e instanceof LibraryException) {
                throw (LibraryException) e;
            }
            throw new DatabaseException("Borrowing transaction failed: " + e.getMessage(), e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    /**
     * CRITICAL MULTI-STEP TRANSACTION: Return Book
     * Rules:
     * 1. Validate active loan
     * 2. Calculate overdue days and fine (if any)
     * 3. Mark loan RETURNED
     * 4. Check for WAITING reservations for this book
     * 5. If waiting reservation exists -> copy marked RESERVED, reservation marked READY, notify member!
     * 6. If no reservation -> copy marked AVAILABLE
     * 7. If fine applicable -> record fine & fine notification
     * 8. Create return confirmation notification
     * 9. Create audit log
     * 10. Commit transaction
     */
    public Fine returnBook(Long loanId, Long actorUserId) {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new ResourceNotFoundException("Loan record not found: " + loanId);
        }

        if (LoanStatus.RETURNED.equals(loan.getStatus())) {
            throw new ConflictException("This book has already been returned.");
        }

        BookCopy copy = bookCopyDAO.findById(loan.getCopyId());
        if (copy == null) {
            throw new ResourceNotFoundException("Copy record not found for loan: " + loanId);
        }

        Book book = bookDAO.findById(copy.getBookId());
        Member member = memberDAO.findById(loan.getMemberId());

        long daysOverdue = loan.getDaysOverdue();
        BigDecimal fineAmount = BigDecimal.ZERO;
        if (daysOverdue > 0) {
            fineAmount = LibraryPolicy.DAILY_FINE_RATE.multiply(BigDecimal.valueOf(daysOverdue));
        }

        Connection conn = null;
        Fine assessedFine = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            Timestamp now = new Timestamp(System.currentTimeMillis());

            // 1. Mark loan as RETURNED
            loanDAO.updateReturn(conn, loanId, now);

            // 2. Assess Fine if overdue
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                assessedFine = new Fine();
                assessedFine.setLoanId(loanId);
                assessedFine.setMemberId(loan.getMemberId());
                assessedFine.setAmount(fineAmount);
                assessedFine.setReason("Overdue penalty: " + daysOverdue + " days late at $" + LibraryPolicy.DAILY_FINE_RATE + "/day");
                assessedFine.setStatus(FineStatus.UNPAID);

                Long fineId = fineDAO.create(conn, assessedFine);
                assessedFine.setId(fineId);

                notificationDAO.create(conn, new Notification(
                        loan.getMemberId(),
                        "Overdue Fine Assessed",
                        "A fine of $" + fineAmount + " has been assessed for returning '" + book.getTitle() + "' " + daysOverdue + " days late.",
                        NotificationType.FINE_CREATED
                ));
            }

            // 3. Process Reservation Queue
            Reservation waitingRes = reservationDAO.findEarliestWaitingReservation(conn, copy.getBookId());
            if (waitingRes != null) {
                // Reserve copy for the next member in queue
                bookCopyDAO.updateStatus(conn, copy.getId(), CopyStatus.RESERVED);
                Timestamp holdExpiry = new Timestamp(System.currentTimeMillis() + (LibraryPolicy.RESERVATION_EXPIRY_HOURS * 3600 * 1000L));
                reservationDAO.updateStatus(conn, waitingRes.getId(), ReservationStatus.READY, holdExpiry);

                // Notify next member
                notificationDAO.create(conn, new Notification(
                        waitingRes.getMemberId(),
                        "Reservation Ready: " + book.getTitle(),
                        "Good news! Your reserved book '" + book.getTitle() + "' is now ready for pickup at the circulation desk. Hold expires in " + LibraryPolicy.RESERVATION_EXPIRY_HOURS + " hours.",
                        NotificationType.RESERVATION_READY
                ));
                logger.info("Reservation {} transitioned to READY for member {}", waitingRes.getId(), waitingRes.getMemberId());
            } else {
                // No pending reservation -> copy is immediately available
                bookCopyDAO.updateStatus(conn, copy.getId(), CopyStatus.AVAILABLE);
            }

            // 4. Return confirmation notification
            notificationDAO.create(conn, new Notification(
                    loan.getMemberId(),
                    "Book Return Confirmed: " + book.getTitle(),
                    "Copy " + copy.getAccessionNumber() + " has been checked in successfully." +
                            (daysOverdue > 0 ? " (Overdue fine assessed: $" + fineAmount + ")" : ""),
                    NotificationType.RETURN_CONFIRMATION
            ));

            // 5. Audit Log
            auditLogDAO.create(conn, new AuditLog(
                    actorUserId,
                    "BOOK_RETURNED",
                    "LOAN",
                    loanId,
                    "Returned copy " + copy.getAccessionNumber() + " of '" + book.getTitle() + "'" +
                            (daysOverdue > 0 ? " [Late: " + daysOverdue + " days, Fine: $" + fineAmount + "]" : "")
            ));

            conn.commit();
            logger.info("Loan {} successfully returned. Fine assessed: {}", loanId, fineAmount);
            return assessedFine;
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Return transaction rolled back for loan {}", loanId, e);
            throw new DatabaseException("Return transaction failed: " + e.getMessage(), e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    /**
     * Renew an eligible loan
     */
    public Loan renewLoan(Long loanId, Long memberId) {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new ResourceNotFoundException("Loan record not found: " + loanId);
        }

        if (!loan.getMemberId().equals(memberId)) {
            throw new AuthorizationException("You are not authorized to renew this loan.");
        }

        if (!loan.isActive()) {
            throw new ConflictException("Only active loans can be renewed.");
        }

        if (loan.isOverdue()) {
            throw new ConflictException("Overdue loans cannot be renewed. Please return the book and pay any accumulated fines.");
        }

        if (loan.getRenewalCount() >= LibraryPolicy.MAX_RENEWALS) {
            throw new ConflictException("Maximum renewals limit reached (" + LibraryPolicy.MAX_RENEWALS + " renewals).");
        }

        // Check if other members are waiting in reservation queue for this book
        BookCopy copy = bookCopyDAO.findById(loan.getCopyId());
        List<Reservation> activeReservations = reservationDAO.findActiveByBookId(copy.getBookId());
        if (!activeReservations.isEmpty()) {
            throw new ConflictException("Cannot renew because another member has reserved this title.");
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            LocalDate newDueDate = loan.getDueDate().plusDays(LibraryPolicy.DEFAULT_LOAN_DAYS);
            int newCount = loan.getRenewalCount() + 1;

            loanDAO.updateDueDate(conn, loanId, newDueDate, newCount);

            notificationDAO.create(conn, new Notification(
                    memberId,
                    "Loan Renewed: " + loan.getBookTitle(),
                    "Your loan has been extended to " + newDueDate + ". (Renewal " + newCount + " of " + LibraryPolicy.MAX_RENEWALS + ").",
                    NotificationType.GENERAL
            ));

            auditLogDAO.create(conn, new AuditLog(
                    memberDAO.findById(memberId).getUserId(),
                    "LOAN_RENEWED",
                    "LOAN",
                    loanId,
                    "Renewed loan " + loanId + " until " + newDueDate
            ));

            conn.commit();
            logger.info("Loan {} renewed until {}", loanId, newDueDate);
            return loanDAO.findById(loanId);
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Failed to renew loan {}", loanId, e);
            throw new DatabaseException("Renewal transaction failed", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public List<Loan> getActiveLoans(Long memberId) {
        return loanDAO.findActiveLoansByMemberId(memberId);
    }

    public List<Loan> getMemberLoanHistory(Long memberId) {
        return loanDAO.findAllLoansByMemberId(memberId);
    }

    public List<Loan> getAllLoans(String statusFilter) {
        return loanDAO.findAll(statusFilter);
    }

    public List<Loan> getOverdueLoans() {
        return loanDAO.findOverdueLoans();
    }
}
