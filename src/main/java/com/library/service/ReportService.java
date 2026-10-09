package com.library.service;

import com.library.dao.*;
import com.library.dao.impl.*;
import com.library.model.Book;
import com.library.model.CopyStatus;
import com.library.model.Loan;
import com.library.model.LoanStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final BookDAO bookDAO;
    private final BookCopyDAO bookCopyDAO;
    private final MemberDAO memberDAO;
    private final LoanDAO loanDAO;
    private final ReservationDAO reservationDAO;
    private final FineDAO fineDAO;
    private final ReadingRoomDAO readingRoomDAO;

    public ReportService() {
        this.bookDAO = new BookDAOImpl();
        this.bookCopyDAO = new BookCopyDAOImpl();
        this.memberDAO = new MemberDAOImpl();
        this.loanDAO = new LoanDAOImpl();
        this.reservationDAO = new ReservationDAOImpl();
        this.fineDAO = new FineDAOImpl();
        this.readingRoomDAO = new ReadingRoomDAOImpl();
    }

    public Map<String, Object> getLibrarianDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalBooks", bookDAO.countTotalBooks());
        metrics.put("totalCopies", bookCopyDAO.countTotalCopies());
        metrics.put("availableCopies", bookCopyDAO.countCopiesByStatus(CopyStatus.AVAILABLE));
        metrics.put("borrowedCopies", bookCopyDAO.countCopiesByStatus(CopyStatus.BORROWED));
        metrics.put("activeMembers", memberDAO.countActiveMembers());
        metrics.put("activeLoans", loanDAO.countLoansByStatus(LoanStatus.ACTIVE));
        metrics.put("overdueLoans", loanDAO.findOverdueLoans().size());
        metrics.put("waitingReservations", reservationDAO.countWaitingReservations());
        metrics.put("totalUnpaidFines", fineDAO.getTotalUnpaidFines());
        metrics.put("todayBookings", readingRoomDAO.countTodayBookings());
        return metrics;
    }

    public List<Book> getPopularBooks(int limit) {
        return bookDAO.findTopPopularBooks(limit);
    }

    public List<Loan> getOverdueReport() {
        return loanDAO.findOverdueLoans();
    }

    /**
     * Server-side CSV generator for Inventory (Rubric Section 41)
     */
    public String generateInventoryCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Book ID,Title,ISBN,Genre,Publisher,Year,Total Copies,Available Copies,Status\n");
        List<Book> books = bookDAO.findAll(false);
        for (Book b : books) {
            csv.append(b.getId()).append(",")
                    .append(escapeCsv(b.getTitle())).append(",")
                    .append(escapeCsv(b.getIsbn())).append(",")
                    .append(escapeCsv(b.getGenreName())).append(",")
                    .append(escapeCsv(b.getPublisher())).append(",")
                    .append(b.getPublicationYear() != null ? b.getPublicationYear() : "").append(",")
                    .append(b.getTotalCopies()).append(",")
                    .append(b.getAvailableCopies()).append(",")
                    .append(b.getStatus()).append("\n");
        }
        return csv.toString();
    }

    /**
     * Server-side CSV generator for Overdue Loans (Rubric Section 41)
     */
    public String generateOverdueCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Loan ID,Member Name,Email,Book Title,Accession Number,Due Date,Days Overdue,Estimated Fine\n");
        List<Loan> overdueLoans = loanDAO.findOverdueLoans();
        for (Loan l : overdueLoans) {
            long daysLate = l.getDaysOverdue();
            BigDecimal fine = BigDecimal.valueOf(daysLate).multiply(new BigDecimal("2.50"));
            csv.append(l.getId()).append(",")
                    .append(escapeCsv(l.getMemberName())).append(",")
                    .append(escapeCsv(l.getMemberEmail())).append(",")
                    .append(escapeCsv(l.getBookTitle())).append(",")
                    .append(escapeCsv(l.getAccessionNumber())).append(",")
                    .append(l.getDueDate()).append(",")
                    .append(daysLate).append(",")
                    .append("$").append(fine).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
