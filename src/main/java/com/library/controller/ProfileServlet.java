package com.library.controller;

import com.library.exception.AuthenticationException;
import com.library.exception.ValidationException;
import com.library.model.Member;
import com.library.model.User;
import com.library.service.AuthService;
import com.library.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/member/profile")
public class ProfileServlet extends BaseServlet {
    private final MemberService memberService = new MemberService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        Member fresh = memberService.getMemberById(member.getId());
        request.setAttribute("member", fresh);
        forward(request, response, "/WEB-INF/views/member/profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        User user = getCurrentUser(request);
        if (member == null || user == null) {
            redirect(request, response, "/login");
            return;
        }

        String action = request.getParameter("action");

        if ("changePassword".equalsIgnoreCase(action)) {
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (newPassword == null || !newPassword.equals(confirmPassword)) {
                setFlashError(request, "New password and confirmation do not match.");
                redirect(request, response, "/member/profile");
                return;
            }

            try {
                authService.changePassword(user.getId(), currentPassword, newPassword);
                setFlashSuccess(request, "Your password has been changed successfully.");
            } catch (AuthenticationException | ValidationException e) {
                setFlashError(request, e.getMessage());
            }
        } else {
            // Update profile
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");
            String address = request.getParameter("address");

            try {
                memberService.updateProfile(member.getId(), fullName, phone, address);
                Member updated = memberService.getMemberById(member.getId());
                HttpSession session = request.getSession();
                session.setAttribute("currentMember", updated);
                setFlashSuccess(request, "Profile updated successfully.");
            } catch (Exception e) {
                setFlashError(request, "Profile update failed: " + e.getMessage());
            }
        }

        redirect(request, response, "/member/profile");
    }
}
