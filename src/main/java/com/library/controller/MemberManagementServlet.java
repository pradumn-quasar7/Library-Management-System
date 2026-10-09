package com.library.controller;

import com.library.model.Member;
import com.library.service.LoanService;
import com.library.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/librarian/members")
public class MemberManagementServlet extends BaseServlet {
    private final MemberService memberService = new MemberService();
    private final LoanService loanService = new LoanService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        String idParam = request.getParameter("id");

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(idParam.trim());
                Member member = memberService.getMemberById(id);
                request.setAttribute("member", member);
                request.setAttribute("activeLoans", loanService.getActiveLoans(id));
                request.setAttribute("history", loanService.getMemberLoanHistory(id));
                forward(request, response, "/WEB-INF/views/librarian/member-detail.jsp");
                return;
            } catch (Exception e) {
                setFlashError(request, "Member not found.");
            }
        }

        List<Member> members = (query != null && !query.trim().isEmpty()) ?
                memberService.searchMembers(query.trim()) : memberService.getAllMembers();

        request.setAttribute("members", members);
        request.setAttribute("query", query);
        forward(request, response, "/WEB-INF/views/librarian/members-list.jsp");
    }
}
