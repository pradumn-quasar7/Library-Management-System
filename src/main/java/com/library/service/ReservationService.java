package com.library.service;

import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.exception.ConflictException;
import com.library.exception.DatabaseException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.*;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationDAO reservationDAO;
    private final BookDAO bookDAO;
    private final BookCopyDAO bookCopyDAO;
    private final MemberDAO memberDAO;
    private final NotificationDAO notificationDAO;
    private final AuditLogDAO auditLogDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.bookCopyDAO = new BookCopyDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.notificationDAO = new NotificationDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    public ReservationService(ReservationDAO reservationDAO, BookDAO bookDAO, BookCopyDAO bookCopyDAO,
                              MemberDAO memberDAO, NotificationDAO notificationDAO, AuditLogDAO auditLogDAO) {
        this.reservationDAO = reservationDAO;
        this.bookDAO = bookDAO;
        this.bookCopyDAO = bookCopyDAO;
        this.memberDAO = memberDAO;
        this.notificationDAO = notificationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Reservation reserveBook(Long memberId, Long bookId) {
        Member member = memberDAO.findById(memberId);
        if (member == null || !member.isActive()) {
            throw new ConflictException("Your membership must be active to reserve books.");
        }

        Book book = bookDAO.findById(bookId);
        if (book == null || !book.isActive()) {
            throw new ResourceNotFoundException("Book not found or inactive.");
        }

        if (book.getAvailableCopies() > 0) {
            throw new ConflictException("This book currently has available copies. You can borrow it directly without reserving.");
        }

        if (reservationDAO.isAlreadyReservedByMember(bookId, memberId)) {
            throw new ConflictException("You already have an active reservation for '" + book.getTitle() + "'.");
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            int queuePosition = reservationDAO.getNextQueuePosition(conn, bookId);

            Reservation res = new Reservation();
            res.setBookId(bookId);
            res.setMemberId(memberId);
            res.setReservedAt(new Timestamp(System.currentTimeMillis()));
            res.setQueuePosition(queuePosition);
            res.setStatus(ReservationStatus.WAITING);

            Long resId = reservationDAO.create(conn, res);
            res.setId(resId);

            notificationDAO.create(conn, new Notification(
                    memberId,
                    "Reservation Confirmed: " + book.getTitle(),
                    "You have reserved '" + book.getTitle() + "'. Your position in the waiting queue is #" + queuePosition + ". We will notify you when a copy becomes ready.",
                    NotificationType.GENERAL
            ));

            auditLogDAO.create(conn, new AuditLog(
                    member.getUserId(),
                    "BOOK_RESERVED",
                    "RESERVATION",
                    resId,
                    "Member " + member.getFullName() + " reserved '" + book.getTitle() + "' at queue position #" + queuePosition
            ));

            conn.commit();
            logger.info("Reservation {} created for member {} on book {} (queue: {})", resId, memberId, bookId, queuePosition);
            return reservationDAO.findById(resId);
        } catch (SQLException e) {
            ConnectionManager.rollbackQuietly(conn);
            logger.error("Failed to create reservation", e);
            throw new DatabaseException("Failed to reserve book", e);
        } finally {
            ConnectionManager.closeQuietly(conn);
        }
    }

    public void cancelReservation(Long reservationId, Long memberId) {
        Reservation res = reservationDAO.findById(reservationId);
        if (res == null) {
            throw new ResourceNotFoundException("Reservation not found.");
        }

        if (!res.getMemberId().equals(memberId)) {
            throw new ConflictException("You are not authorized to cancel this reservation.");
        }

        boolean cancelled = reservationDAO.cancel(reservationId, memberId);
        if (!cancelled) {
            throw new ConflictException("Unable to cancel reservation (it may have already expired or fulfilled).");
        }

        Member member = memberDAO.findById(memberId);
        auditLogDAO.create(new AuditLog(member.getUserId(), "RESERVATION_CANCELLED", "RESERVATION", reservationId, "Member cancelled reservation for book " + res.getBookTitle()));
        logger.info("Reservation {} cancelled by member {}", reservationId, memberId);
    }

    public List<Reservation> getMemberReservations(Long memberId) {
        return reservationDAO.findByMemberId(memberId);
    }

    public List<Reservation> getBookReservations(Long bookId) {
        return reservationDAO.findActiveByBookId(bookId);
    }

    /**
     * Called by background scheduler to expire unclaimed READY reservations and advance the queue!
     */
    public void processExpiredReservations() {
        List<Reservation> expiredList = reservationDAO.findExpiredReadyReservations();
        for (Reservation expired : expiredList) {
            logger.info("Expiring reservation {} for book {}", expired.getId(), expired.getBookTitle());
            Connection conn = null;
            try {
                conn = ConnectionManager.getConnection();
                conn.setAutoCommit(false);

                // Mark expired
                reservationDAO.updateStatus(conn, expired.getId(), ReservationStatus.EXPIRED, null);

                // Check next waiting reservation
                Reservation nextWaiting = reservationDAO.findEarliestWaitingReservation(conn, expired.getBookId());
                if (nextWaiting != null) {
                    Timestamp holdExpiry = new Timestamp(System.currentTimeMillis() + (48 * 3600 * 1000L));
                    reservationDAO.updateStatus(conn, nextWaiting.getId(), ReservationStatus.READY, holdExpiry);

                    notificationDAO.create(conn, new Notification(
                            nextWaiting.getMemberId(),
                            "Reservation Ready: " + expired.getBookTitle(),
                            "A copy of '" + expired.getBookTitle() + "' has become available from an unclaimed hold. Pickup hold is active for 48 hours.",
                            NotificationType.RESERVATION_READY
                    ));
                } else {
                    // No one else waiting -> set one copy back to AVAILABLE
                    BookCopy copy = bookCopyDAO.findCopiesByBookId(expired.getBookId()).stream()
                            .filter(c -> CopyStatus.RESERVED.equals(c.getStatus()))
                            .findFirst()
                            .orElse(null);
                    if (copy != null) {
                        bookCopyDAO.updateStatus(conn, copy.getId(), CopyStatus.AVAILABLE);
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                ConnectionManager.rollbackQuietly(conn);
                logger.error("Error processing expired reservation {}", expired.getId(), e);
            } finally {
                ConnectionManager.closeQuietly(conn);
            }
        }
    }
}
