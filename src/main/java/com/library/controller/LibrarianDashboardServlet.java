package com.library.controller;

import com.library.dao.AuditLogDAO;
import com.library.dao.LoanDAO;
import com.library.dao.impl.AuditLogDAOImpl;
import com.library.dao.impl.LoanDAOImpl;
import com.library.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/librarian/dashboard")
public class LibrarianDashboardServlet extends BaseServlet {
    private final ReportService reportService = new ReportService();
    private final LoanDAO loanDAO = new LoanDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> metrics = reportService.getLibrarianDashboardMetrics();
        request.setAttribute("metrics", metrics);
        request.setAttribute("popularBooks", reportService.getPopularBooks(5));
        request.setAttribute("overdueLoans", loanDAO.findOverdueLoans());
        request.setAttribute("recentLoans", loanDAO.findAll("ALL").stream().limit(8).toList());
        request.setAttribute("recentAuditLogs", auditLogDAO.findRecentLogs(8));

        forward(request, response, "/WEB-INF/views/librarian/dashboard.jsp");
    }
}
