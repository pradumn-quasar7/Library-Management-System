package com.library.dao;

import com.library.model.Reservation;
import com.library.model.ReservationStatus;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

public interface ReservationDAO {
    Reservation findById(Long id);
    Reservation findById(Connection conn, Long id);
    List<Reservation> findByMemberId(Long memberId);
    List<Reservation> findActiveByBookId(Long bookId);
    Reservation findEarliestWaitingReservation(Connection conn, Long bookId);
    Long create(Connection conn, Reservation reservation);
    boolean updateStatus(Connection conn, Long reservationId, ReservationStatus status, Timestamp expiresAt);
    boolean cancel(Long reservationId, Long memberId);
    boolean isAlreadyReservedByMember(Long bookId, Long memberId);
    int getNextQueuePosition(Connection conn, Long bookId);
    int countWaitingReservations();
    List<Reservation> findExpiredReadyReservations();
}
