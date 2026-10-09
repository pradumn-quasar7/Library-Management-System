package com.library.controller;

import com.library.model.Member;
import com.library.model.ReadingRoomBooking;
import com.library.model.ReadingRoomSeat;
import com.library.service.ReadingRoomService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/member/reading-room")
public class ReadingRoomBookingServlet extends BaseServlet {
    private final ReadingRoomService readingRoomService = new ReadingRoomService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        List<ReadingRoomSeat> seats = readingRoomService.getAllSeats();
        List<ReadingRoomBooking> myBookings = readingRoomService.getMemberBookings(member.getId());

        request.setAttribute("seats", seats);
        request.setAttribute("myBookings", myBookings);
        request.setAttribute("today", LocalDate.now());

        forward(request, response, "/WEB-INF/views/member/reading-room.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("cancel".equalsIgnoreCase(action)) {
            String bookingIdParam = request.getParameter("bookingId");
            if (bookingIdParam != null) {
                try {
                    Long bookingId = Long.parseLong(bookingIdParam.trim());
                    readingRoomService.cancelBooking(bookingId, member.getId());
                    setFlashSuccess(request, "Reading room booking cancelled successfully.");
                } catch (Exception e) {
                    setFlashError(request, "Cancellation failed: " + e.getMessage());
                }
            }
            redirect(request, response, "/member/reading-room");
            return;
        }

        // Book action
        String seatIdParam = request.getParameter("seatId");
        String dateParam = request.getParameter("date");
        String startParam = request.getParameter("startTime");
        String endParam = request.getParameter("endTime");

        try {
            Long seatId = Long.parseLong(seatIdParam);
            LocalDate date = LocalDate.parse(dateParam);
            Time startTime = Time.valueOf(startParam.length() == 5 ? startParam + ":00" : startParam);
            Time endTime = Time.valueOf(endParam.length() == 5 ? endParam + ":00" : endParam);

            ReadingRoomBooking booking = readingRoomService.bookSeat(member.getId(), seatId, date, startTime, endTime);
            setFlashSuccess(request, "Reading room seat successfully booked for " + date + " (" + startParam + " - " + endParam + ")!");
        } catch (Exception e) {
            setFlashError(request, "Booking failed: " + e.getMessage());
        }

        redirect(request, response, "/member/reading-room");
    }
}
