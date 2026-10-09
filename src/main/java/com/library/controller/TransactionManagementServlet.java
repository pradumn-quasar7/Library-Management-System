package com.library.controller;

import com.library.model.Loan;
import com.library.service.LoanService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/librarian/transactions")
public class TransactionManagementServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filter = request.getParameter("status");
        if (filter == null || filter.trim().isEmpty()) {
            filter = "ALL";
        }

        List<Loan> loans = loanService.getAllLoans(filter);
        request.setAttribute("loans", loans);
        request.setAttribute("currentFilter", filter);

        forward(request, response, "/WEB-INF/views/librarian/transactions.jsp");
    }
}
