package com.library.controller;

import com.library.model.Fine;
import com.library.model.User;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/transactions/return")
public class LibrarianReturnServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        String loanIdParam = request.getParameter("loanId");

        if (loanIdParam != null && !loanIdParam.trim().isEmpty()) {
            try {
                Long loanId = Long.parseLong(loanIdParam.trim());
                Fine fine = loanService.returnBook(loanId, user != null ? user.getId() : null);
                if (fine != null) {
                    setFlashSuccess(request, "Book returned successfully. Overdue fine of $" + fine.getAmount() + " assessed to member.");
                } else {
                    setFlashSuccess(request, "Book returned successfully with zero fines.");
                }
            } catch (Exception e) {
                setFlashError(request, "Failed to return book: " + e.getMessage());
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
