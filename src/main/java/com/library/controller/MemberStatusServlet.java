package com.library.controller;

import com.library.model.User;
import com.library.model.UserStatus;
import com.library.service.MemberService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/members/status")
public class MemberStatusServlet extends BaseServlet {
    private final MemberService memberService = new MemberService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        String idParam = request.getParameter("id");
        String statusStr = request.getParameter("status");

        if (idParam != null && statusStr != null) {
            try {
                Long id = Long.parseLong(idParam.trim());
                UserStatus status = UserStatus.fromString(statusStr);
                if (status != null) {
                    memberService.setMemberStatus(id, status, user != null ? user.getId() : null);
                    setFlashSuccess(request, "Member account status updated to " + status + ".");
                }
            } catch (Exception e) {
                setFlashError(request, "Failed to update member status: " + e.getMessage());
            }
        }
        redirect(request, response, "/librarian/members");
    }
}
