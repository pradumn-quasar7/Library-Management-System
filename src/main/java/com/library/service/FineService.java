package com.library.service;

import com.library.dao.AuditLogDAO;
import com.library.dao.FineDAO;
import com.library.dao.MemberDAO;
import com.library.dao.NotificationDAO;
import com.library.dao.impl.AuditLogDAOImpl;
import com.library.dao.impl.FineDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.dao.impl.NotificationDAOImpl;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.*;
import com.library.validation.LibraryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class FineService {
    private static final Logger logger = LoggerFactory.getLogger(FineService.class);

    private final FineDAO fineDAO;
    private final MemberDAO memberDAO;
    private final NotificationDAO notificationDAO;
    private final AuditLogDAO auditLogDAO;

    public FineService() {
        this.fineDAO = new FineDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public FineService(FineDAO fineDAO, MemberDAO memberDAO, NotificationDAO notificationDAO, AuditLogDAO auditLogDAO) {
        this.fineDAO = fineDAO;
        this.memberDAO = memberDAO;
        this.notificationDAO = notificationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Fine> getMemberFines(Long memberId) {
        return fineDAO.findByMemberId(memberId);
    }

    public List<Fine> getAllFines(String statusFilter) {
        return fineDAO.findAll(statusFilter);
    }

    public BigDecimal getTotalUnpaidFines() {
        return fineDAO.getTotalUnpaidFines();
    }

    public BigDecimal getMemberUnpaidFines(Long memberId) {
        return fineDAO.getUnpaidFinesByMemberId(memberId);
    }

    public BigDecimal calculateFine(LocalDate dueDate, LocalDate returnedDate) {
        if (dueDate == null || returnedDate == null || !returnedDate.isAfter(dueDate)) {
            return BigDecimal.ZERO;
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnedDate);
        return LibraryPolicy.DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));
    }

    public void payFine(Long fineId, Long memberId) {
        Fine fine = fineDAO.findById(fineId);
        if (fine == null) {
            throw new ResourceNotFoundException("Fine record not found: " + fineId);
        }

        if (!fine.getMemberId().equals(memberId)) {
            throw new ConflictException("You are not authorized to pay this fine.");
        }

        if (!FineStatus.UNPAID.equals(fine.getStatus())) {
            throw new ConflictException("This fine is already " + fine.getStatus() + ".");
        }

        fineDAO.updateStatus(fineId, FineStatus.PAID, new Timestamp(System.currentTimeMillis()));

        notificationDAO.create(new Notification(
                memberId,
                "Fine Payment Confirmed",
                "Payment of $" + fine.getAmount() + " for fine #" + fineId + " has been processed successfully. Thank you!",
                NotificationType.GENERAL
        ));

        Member member = memberDAO.findById(memberId);
        auditLogDAO.create(new AuditLog(member.getUserId(), "FINE_PAID", "FINE", fineId, "Member paid fine of $" + fine.getAmount()));
        logger.info("Fine {} of ${} paid by member {}", fineId, fine.getAmount(), memberId);
    }

    public void waiveFine(Long fineId, Long librarianUserId) {
        Fine fine = fineDAO.findById(fineId);
        if (fine == null) {
            throw new ResourceNotFoundException("Fine record not found: " + fineId);
        }

        if (!FineStatus.UNPAID.equals(fine.getStatus())) {
            throw new ConflictException("Cannot waive a fine that is already " + fine.getStatus() + ".");
        }

        fineDAO.updateStatus(fineId, FineStatus.WAIVED, new Timestamp(System.currentTimeMillis()));

        notificationDAO.create(new Notification(
                fine.getMemberId(),
                "Fine Waived",
                "Your fine of $" + fine.getAmount() + " for '" + fine.getBookTitle() + "' has been waived by the librarian.",
                NotificationType.GENERAL
        ));

        auditLogDAO.create(new AuditLog(librarianUserId, "FINE_WAIVED", "FINE", fineId, "Librarian waived fine of $" + fine.getAmount() + " for member #" + fine.getMemberId()));
        logger.info("Fine {} waived by librarian {}", fineId, librarianUserId);
    }
}
