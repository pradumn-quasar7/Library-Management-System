package com.library.controller;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/member/dashboard")
public class MemberDashboardServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();
    private final ReservationService reservationService = new ReservationService();
    private final FineService fineService = new FineService();
    private final RecommendationService recommendationService = new RecommendationService();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        List<Loan> activeLoans = loanService.getActiveLoans(member.getId());
        List<Reservation> reservations = reservationService.getMemberReservations(member.getId());
        BigDecimal unpaidFines = fineService.getMemberUnpaidFines(member.getId());

        // Overdue risk assessment (Section 7.3)
        boolean hasOverdue = activeLoans.stream().anyMatch(Loan::isOverdue);
        boolean dueSoon = activeLoans.stream().anyMatch(l -> l.getDaysRemaining() >= 0 && l.getDaysRemaining() <= 2);
        String riskStatus = hasOverdue ? "OVERDUE" : (dueSoon ? "ATTENTION" : "NORMAL");

        request.setAttribute("activeLoans", activeLoans);
        request.setAttribute("reservations", reservations);
        request.setAttribute("unpaidFines", unpaidFines);
        request.setAttribute("riskStatus", riskStatus);
        request.setAttribute("recommendations", recommendationService.getRecommendationsForMember(member.getId(), 4));
        request.setAttribute("notifications", notificationService.getMemberNotifications(member.getId(), 5));
        request.setAttribute("unreadCount", notificationService.getUnreadCount(member.getId()));

        forward(request, response, "/WEB-INF/views/member/dashboard.jsp");
    }
}
