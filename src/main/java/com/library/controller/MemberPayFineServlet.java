package com.library.controller;

import com.library.model.Member;
import com.library.service.FineService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/fines/pay")
public class MemberPayFineServlet extends BaseServlet {
    private final FineService fineService = new FineService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String fineIdParam = request.getParameter("fineId");
        if (fineIdParam != null && !fineIdParam.trim().isEmpty()) {
            try {
                Long fineId = Long.parseLong(fineIdParam.trim());
                fineService.payFine(fineId, member.getId());
                setFlashSuccess(request, "Fine #" + fineId + " has been paid successfully.");
            } catch (Exception e) {
                setFlashError(request, "Payment failed: " + e.getMessage());
            }
        }
        redirect(request, response, "/member/dashboard");
    }
}
