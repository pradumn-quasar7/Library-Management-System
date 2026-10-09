package com.library.controller;

import com.library.service.ReportService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@WebServlet("/librarian/reports/export")
public class ReportExportServlet extends BaseServlet {
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String type = request.getParameter("type"); // inventory or overdue

        String csvContent;
        String filename;

        if ("overdue".equalsIgnoreCase(type)) {
            csvContent = reportService.generateOverdueCsv();
            filename = "library-overdue-report-" + System.currentTimeMillis() + ".csv";
        } else {
            csvContent = reportService.generateInventoryCsv();
            filename = "library-inventory-report-" + System.currentTimeMillis() + ".csv";
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }
}
