package com.library.controller;

import com.library.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/reports")
public class ReportsServlet extends BaseServlet {
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("metrics", reportService.getLibrarianDashboardMetrics());
        request.setAttribute("popularBooks", reportService.getPopularBooks(10));
        request.setAttribute("overdueLoans", reportService.getOverdueReport());

        forward(request, response, "/WEB-INF/views/librarian/reports.jsp");
    }
}
