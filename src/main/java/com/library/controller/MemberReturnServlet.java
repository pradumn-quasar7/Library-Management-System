package com.library.controller;

import com.library.model.Fine;
import com.library.model.Member;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/return")
public class MemberReturnServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String loanIdParam = request.getParameter("loanId");
        if (loanIdParam != null && !loanIdParam.trim().isEmpty()) {
            try {
                Long loanId = Long.parseLong(loanIdParam.trim());
                Fine fine = loanService.returnBook(loanId, member.getUserId());
                if (fine != null) {
                    setFlashSuccess(request, "Book returned successfully. Late return penalty assessed: $" + fine.getAmount() + ".");
                } else {
                    setFlashSuccess(request, "Book returned successfully. Thank you for returning on time!");
                }
            } catch (Exception e) {
                setFlashError(request, "Return failed: " + e.getMessage());
            }
        }
        redirect(request, response, "/member/loans");
    }
}
