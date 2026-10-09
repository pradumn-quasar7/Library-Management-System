package com.library.controller;

import com.library.model.Member;
import com.library.model.NotificationPreference;
import com.library.service.NotificationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/notifications/preferences")
public class NotificationPreferencesServlet extends BaseServlet {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        boolean dueAlert = "on".equalsIgnoreCase(request.getParameter("dueDateAlert")) || "true".equalsIgnoreCase(request.getParameter("dueDateAlert"));
        boolean overdueAlert = "on".equalsIgnoreCase(request.getParameter("overdueAlert")) || "true".equalsIgnoreCase(request.getParameter("overdueAlert"));
        boolean newArrivalAlert = "on".equalsIgnoreCase(request.getParameter("newArrivalAlert")) || "true".equalsIgnoreCase(request.getParameter("newArrivalAlert"));
        boolean reservationAlert = "on".equalsIgnoreCase(request.getParameter("reservationAlert")) || "true".equalsIgnoreCase(request.getParameter("reservationAlert"));

        NotificationPreference pref = new NotificationPreference(member.getId(), dueAlert, overdueAlert, newArrivalAlert, reservationAlert);
        notificationService.updatePreferences(pref);

        setFlashSuccess(request, "Notification preferences updated successfully.");
        redirect(request, response, "/member/notifications");
    }
}
