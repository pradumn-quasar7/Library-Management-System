package com.library.controller;

import com.library.model.Member;
import com.library.model.Notification;
import com.library.model.NotificationPreference;
import com.library.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/member/notifications")
public class NotificationsServlet extends BaseServlet {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        List<Notification> notifications = notificationService.getMemberNotifications(member.getId(), 50);
        NotificationPreference preferences = notificationService.getPreferences(member.getId());

        request.setAttribute("notifications", notifications);
        request.setAttribute("preferences", preferences);
        forward(request, response, "/WEB-INF/views/member/notifications.jsp");
    }
}
