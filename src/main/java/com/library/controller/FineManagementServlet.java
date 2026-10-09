package com.library.controller;

import com.library.model.Fine;
import com.library.model.User;
import com.library.service.FineService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/librarian/fines")
public class FineManagementServlet extends BaseServlet {
    private final FineService fineService = new FineService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = request.getParameter("status");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        List<Fine> fines = fineService.getAllFines(filter);
        request.setAttribute("fines", fines);
        request.setAttribute("currentFilter", filter);
        request.setAttribute("totalUnpaid", fineService.getTotalUnpaidFines());

        forward(request, response, "/WEB-INF/views/librarian/fines.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        String action = request.getParameter("action");
        String fineIdParam = request.getParameter("fineId");

        if ("waive".equalsIgnoreCase(action) && fineIdParam != null) {
            try {
                Long fineId = Long.parseLong(fineIdParam.trim());
                fineService.waiveFine(fineId, user != null ? user.getId() : null);
                setFlashSuccess(request, "Fine #" + fineId + " has been waived.");
            } catch (Exception e) {
                setFlashError(request, "Failed to waive fine: " + e.getMessage());
            }
        }
        redirect(request, response, "/librarian/fines");
    }
}
