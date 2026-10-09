package com.library.controller;

import com.library.model.ReadingRoomBooking;
import com.library.model.ReadingRoomSeat;
import com.library.service.ReadingRoomService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/librarian/reading-room")
public class LibrarianReadingRoomServlet extends BaseServlet {
    private final ReadingRoomService readingRoomService = new ReadingRoomService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dateParam = request.getParameter("date");
        LocalDate date = LocalDate.now();
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try {
                date = LocalDate.parse(dateParam.trim());
            } catch (Exception ignored) {}
        }

        List<ReadingRoomSeat> seats = readingRoomService.getAllSeats();
        List<ReadingRoomBooking> bookings = readingRoomService.getBookingsByDate(date);

        request.setAttribute("seats", seats);
        request.setAttribute("bookings", bookings);
        request.setAttribute("selectedDate", date);

        forward(request, response, "/WEB-INF/views/librarian/reading-room.jsp");
    }
}
