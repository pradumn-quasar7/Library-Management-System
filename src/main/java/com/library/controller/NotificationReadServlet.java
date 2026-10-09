package com.library.controller;

import com.library.model.Member;
import com.library.service.NotificationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/notifications/read")
public class NotificationReadServlet extends BaseServlet {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String idParam = request.getParameter("id");
        String allParam = request.getParameter("all");

        if ("true".equalsIgnoreCase(allParam)) {
            notificationService.markAllAsRead(member.getId());
            setFlashSuccess(request, "All notifications marked as read.");
        } else if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(idParam.trim());
                notificationService.markAsRead(id, member.getId());
            } catch (NumberFormatException ignored) {}
        }

        redirect(request, response, "/member/notifications");
    }
}
