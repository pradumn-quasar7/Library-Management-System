package com.library.controller;

import com.library.dao.MemberDAO;
import com.library.dao.impl.MemberDAOImpl;
import com.library.model.Loan;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/transactions/renew")
public class LibrarianRenewServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();
    private final MemberDAO memberDAO = new MemberDAOImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String loanIdParam = request.getParameter("loanId");

        if (loanIdParam != null && !loanIdParam.trim().isEmpty()) {
            try {
                Long loanId = Long.parseLong(loanIdParam.trim());
                Loan existing = loanService.getAllLoans("ALL").stream()
                        .filter(l -> l.getId().equals(loanId))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    Loan renewed = loanService.renewLoan(loanId, existing.getMemberId());
                    setFlashSuccess(request, "Loan successfully renewed until " + renewed.getDueDate() + ".");
                } else {
                    setFlashError(request, "Loan record not found.");
                }
            } catch (Exception e) {
                setFlashError(request, "Renewal failed: " + e.getMessage());
            }
        }

        String redirectUri = request.getParameter("redirect");
        if (redirectUri != null && !redirectUri.isEmpty()) {
            response.sendRedirect(redirectUri);
        } else {
            redirect(request, response, "/librarian/transactions");
        }
    }
}
