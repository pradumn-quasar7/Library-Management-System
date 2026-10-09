package com.library.model;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

public class ReadingRoomBooking implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long memberId;
    private Long seatId;
    private LocalDate bookingDate;
    private Time startTime;
    private Time endTime;
    private BookingStatus status = BookingStatus.BOOKED;
    private Timestamp createdAt;

    // Transient fields
    private String seatNumber;
    private String memberName;
    private String memberMembershipId;

    public ReadingRoomBooking() {
    }

    public ReadingRoomBooking(Long id, Long memberId, Long seatId, LocalDate bookingDate, Time startTime, Time endTime, BookingStatus status) {
        this.id = id;
        this.memberId = memberId;
        this.seatId = seatId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberMembershipId() {
        return memberMembershipId;
    }

    public void setMemberMembershipId(String memberMembershipId) {
        this.memberMembershipId = memberMembershipId;
    }

    public boolean isBooked() {
        return BookingStatus.BOOKED.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadingRoomBooking that = (ReadingRoomBooking) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ReadingRoomBooking{" +
                "id=" + id +
                ", seatId=" + seatId +
                ", memberId=" + memberId +
                ", bookingDate=" + bookingDate +
                ", status=" + status +
                '}';
    }
}
