package com.library.controller;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/borrow")
public class MemberBorrowServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String bookIdParam = request.getParameter("bookId");
        if (bookIdParam != null && !bookIdParam.trim().isEmpty()) {
            try {
                Long bookId = Long.parseLong(bookIdParam.trim());
                Loan loan = loanService.borrowBook(member.getId(), bookId);
                setFlashSuccess(request, "Book '" + loan.getBookTitle() + "' successfully borrowed! Due date: " + loan.getDueDate() + ".");
                redirect(request, response, "/member/loans");
                return;
            } catch (Exception e) {
                setFlashError(request, "Borrowing failed: " + e.getMessage());
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            response.sendRedirect(referer);
        } else {
            redirect(request, response, "/books/search");
        }
    }
}
