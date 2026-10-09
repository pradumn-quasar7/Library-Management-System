package com.library.model;

import java.io.Serializable;
import java.util.Objects;

public class ReadingRoomSeat implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String seatNumber;
    private SeatStatus status = SeatStatus.AVAILABLE;

    public ReadingRoomSeat() {
    }

    public ReadingRoomSeat(Long id, String seatNumber, SeatStatus status) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return SeatStatus.AVAILABLE.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadingRoomSeat that = (ReadingRoomSeat) o;
        return Objects.equals(id, that.id) || (seatNumber != null && seatNumber.equalsIgnoreCase(that.seatNumber));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seatNumber != null ? seatNumber.toUpperCase() : null);
    }

    @Override
    public String toString() {
        return "ReadingRoomSeat{" +
                "id=" + id +
                ", seatNumber='" + seatNumber + '\'' +
                ", status=" + status +
                '}';
    }
}
