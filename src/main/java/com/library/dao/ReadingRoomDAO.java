package com.library.dao;

import com.library.model.ReadingRoomBooking;
import com.library.model.ReadingRoomSeat;

import java.sql.Connection;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public interface ReadingRoomDAO {
    List<ReadingRoomSeat> findAllSeats();
    ReadingRoomSeat findSeatById(Long id);
    List<ReadingRoomBooking> findBookingsByDate(LocalDate date);
    List<ReadingRoomBooking> findBookingsByMemberId(Long memberId);
    boolean isSeatAvailable(Connection conn, Long seatId, LocalDate date, Time startTime, Time endTime);
    Long createBooking(Connection conn, ReadingRoomBooking booking);
    boolean cancelBooking(Long bookingId, Long memberId);
    int countBookingsByMemberForDate(Long memberId, LocalDate date);
    int countTodayBookings();
}
