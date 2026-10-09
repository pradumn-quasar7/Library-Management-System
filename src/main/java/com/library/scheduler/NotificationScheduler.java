package com.library.scheduler;

import com.library.dao.LoanDAO;
import com.library.dao.impl.LoanDAOImpl;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.NotificationType;
import com.library.service.FineService;
import com.library.service.NotificationService;
import com.library.service.ReservationService;
import com.library.util.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background Scheduler demonstrating Java Multithreading & Concurrency
 * Uses ScheduledExecutorService to run periodic asynchronous tasks:
 * 1. Due date warnings (48 hours before due)
 * 2. Overdue loan detection & automatic status progression
 * 3. Reservation hold expirations and queue advancement
 */
public class NotificationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    private static ScheduledExecutorService scheduler;

    public static synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Library-Background-Scheduler");
            t.setDaemon(true);
            return t;
        });

        logger.info("Initializing Background Scheduled Tasks...");

        // Task 1: Check books due in next 48 hours (Run every hour)
        scheduler.scheduleAtFixedRate(new DueDateNotificationTask(), 1, 60, TimeUnit.MINUTES);

        // Task 2: Check overdue loans and transition statuses (Run every 30 minutes)
        scheduler.scheduleAtFixedRate(new OverdueNotificationTask(), 2, 30, TimeUnit.MINUTES);

        // Task 3: Check expired reservations (Run every 15 minutes)
        scheduler.scheduleAtFixedRate(new ReservationExpiryTask(), 3, 15, TimeUnit.MINUTES);

        logger.info("Library Background Scheduler started successfully.");
    }

    public static synchronized void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            logger.info("Shutting down Background ScheduledExecutorService...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Background ScheduledExecutorService stopped.");
        }
    }

    private static class DueDateNotificationTask implements Runnable {
        private final LoanDAO loanDAO = new LoanDAOImpl();
        private final NotificationService notificationService = new NotificationService();

        @Override
        public void run() {
            try {
                logger.debug("Running DueDateNotificationTask...");
                List<Loan> dueSoonLoans = loanDAO.findLoansDueWithinHours(48);
                for (Loan loan : dueSoonLoans) {
                    notificationService.sendNotification(
                            loan.getMemberId(),
                            "Book Due Soon: " + loan.getBookTitle(),
                            "Your borrowed title '" + loan.getBookTitle() + "' (Copy: " + loan.getAccessionNumber() +
                                    ") is due on " + loan.getDueDate() + ". Please renew or return on time.",
                            NotificationType.DUE_SOON
                    );
                }
            } catch (Exception e) {
                logger.error("Error running DueDateNotificationTask", e);
            }
        }
    }

    private static class OverdueNotificationTask implements Runnable {
        private final LoanDAO loanDAO = new LoanDAOImpl();
        private final NotificationService notificationService = new NotificationService();

        @Override
        public void run() {
            try {
                logger.debug("Running OverdueNotificationTask...");
                List<Loan> overdueLoans = loanDAO.findOverdueLoans();
                for (Loan loan : overdueLoans) {
                    if (LoanStatus.ACTIVE.equals(loan.getStatus())) {
                        Connection conn = null;
                        try {
                            conn = ConnectionManager.getConnection();
                            conn.setAutoCommit(false);
                            loanDAO.updateStatus(conn, loan.getId(), LoanStatus.OVERDUE);
                            conn.commit();
                        } catch (SQLException e) {
                            ConnectionManager.rollbackQuietly(conn);
                        } finally {
                            ConnectionManager.closeQuietly(conn);
                        }
                    }

                    notificationService.sendNotification(
                            loan.getMemberId(),
                            "URGENT: Book Overdue - " + loan.getBookTitle(),
                            "Your borrowed title '" + loan.getBookTitle() + "' was due on " + loan.getDueDate() +
                                    ". It is now " + loan.getDaysOverdue() + " days overdue. Please return immediately to prevent additional fines.",
                            NotificationType.OVERDUE
                    );
                }
            } catch (Exception e) {
                logger.error("Error running OverdueNotificationTask", e);
            }
        }
    }

    private static class ReservationExpiryTask implements Runnable {
        private final ReservationService reservationService = new ReservationService();

        @Override
        public void run() {
            try {
                logger.debug("Running ReservationExpiryTask...");
                reservationService.processExpiredReservations();
            } catch (Exception e) {
                logger.error("Error running ReservationExpiryTask", e);
            }
        }
    }
}
