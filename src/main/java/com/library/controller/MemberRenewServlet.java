package com.library.controller;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.LoanService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/renew")
public class MemberRenewServlet extends BaseServlet {
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
                Loan renewed = loanService.renewLoan(loanId, member.getId());
                setFlashSuccess(request, "Loan for '" + renewed.getBookTitle() + "' successfully renewed until " + renewed.getDueDate() + "!");
            } catch (Exception e) {
                setFlashError(request, "Renewal failed: " + e.getMessage());
            }
        }
        redirect(request, response, "/member/loans");
    }
}
