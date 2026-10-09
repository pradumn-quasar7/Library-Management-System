package com.library.controller;

import com.library.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/audit")
public class AuditLogServlet extends BaseServlet {
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String limitParam = request.getParameter("limit");
        int limit = 100;
        if (limitParam != null && !limitParam.trim().isEmpty()) {
            try {
                limit = Integer.parseInt(limitParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        request.setAttribute("auditLogs", auditService.getRecentLogs(limit));
        forward(request, response, "/WEB-INF/views/librarian/audit.jsp");
    }
}
