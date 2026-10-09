package com.library.controller;

import com.library.exception.AuthenticationException;
import com.library.model.Member;
import com.library.model.Role;
import com.library.model.User;
import com.library.service.AuthService;
import com.library.service.MemberService;
import com.library.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private final AuthService authService = new AuthService();
    private final MemberService memberService = new MemberService();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser != null) {
            if (Role.LIBRARIAN.equals(currentUser.getRole())) {
                redirect(request, response, "/librarian/dashboard");
            } else {
                redirect(request, response, "/member/dashboard");
            }
            return;
        }

        forward(request, response, "/WEB-INF/views/auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = authService.authenticate(email, password);

            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);
            session.setAttribute("userRole", user.getRole().name());

            if (Role.MEMBER.equals(user.getRole())) {
                try {
                    Member member = memberService.getMemberByUserId(user.getId());
                    session.setAttribute("currentMember", member);
                    session.setAttribute("unreadNotificationsCount", notificationService.getUnreadCount(member.getId()));
                } catch (Exception e) {
                    // Member profile might not be found if librarian
                }
            }

            String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
            session.removeAttribute("redirectAfterLogin");

            if (redirectAfterLogin != null && !redirectAfterLogin.contains("/login") && !redirectAfterLogin.contains("/logout")) {
                response.sendRedirect(redirectAfterLogin);
            } else if (Role.LIBRARIAN.equals(user.getRole())) {
                redirect(request, response, "/librarian/dashboard");
            } else {
                redirect(request, response, "/member/dashboard");
            }
        } catch (AuthenticationException e) {
            request.setAttribute("email", email);
            request.setAttribute("errorMessage", e.getMessage());
            forward(request, response, "/WEB-INF/views/auth/login.jsp");
        }
    }
}
