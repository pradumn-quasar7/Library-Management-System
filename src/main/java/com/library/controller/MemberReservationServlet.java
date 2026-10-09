package com.library.controller;

import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/member/reserve")
public class MemberReservationServlet extends BaseServlet {
    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        request.setAttribute("reservations", reservationService.getMemberReservations(member.getId()));
        forward(request, response, "/WEB-INF/views/member/reservations.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String bookIdParam = request.getParameter("bookId");
        if (bookIdParam != null && !bookIdParam.trim().isEmpty()) {
            try {
                Long bookId = Long.parseLong(bookIdParam.trim());
                Reservation res = reservationService.reserveBook(member.getId(), bookId);
                setFlashSuccess(request, "Book '" + res.getBookTitle() + "' successfully reserved! You are at queue position #" + res.getQueuePosition() + ".");
                redirect(request, response, "/member/reserve");
                return;
            } catch (Exception e) {
                setFlashError(request, "Reservation failed: " + e.getMessage());
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            response.sendRedirect(referer);
        } else {
            redirect(request, response, "/books/search");
        }
    }
}
