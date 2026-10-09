package com.library.controller;

import com.library.model.Member;
import com.library.service.ReservationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/reserve/cancel")
public class CancelReservationServlet extends BaseServlet {
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String resIdParam = request.getParameter("reservationId");
        if (resIdParam != null && !resIdParam.trim().isEmpty()) {
            try {
                Long resId = Long.parseLong(resIdParam.trim());
                reservationService.cancelReservation(resId, member.getId());
                setFlashSuccess(request, "Reservation cancelled successfully.");
            } catch (Exception e) {
                setFlashError(request, "Cancellation failed: " + e.getMessage());
            }
        }
        redirect(request, response, "/member/reserve");
    }
}
