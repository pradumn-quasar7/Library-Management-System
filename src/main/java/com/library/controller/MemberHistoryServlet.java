package com.library.controller;

import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.LoanService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/member/history")
public class MemberHistoryServlet extends BaseServlet {
    private final LoanService loanService = new LoanService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        List<Loan> history = loanService.getMemberLoanHistory(member.getId());
        request.setAttribute("history", history);
        forward(request, response, "/WEB-INF/views/member/history.jsp");
    }
}
