package com.library.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class Reservation implements Serializable, Comparable<Reservation> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookId;
    private Long memberId;
    private Timestamp reservedAt;
    private int queuePosition = 1;
    private ReservationStatus status = ReservationStatus.WAITING;
    private Timestamp expiresAt;

    // Transient fields for view display
    private String bookTitle;
    private String bookIsbn;
    private String memberName;
    private String memberEmail;

    public Reservation() {
    }

    public Reservation(Long id, Long bookId, Long memberId, Timestamp reservedAt, int queuePosition, ReservationStatus status) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservedAt = reservedAt;
        this.queuePosition = queuePosition;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Timestamp getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(Timestamp reservedAt) {
        this.reservedAt = reservedAt;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public boolean isWaiting() {
        return ReservationStatus.WAITING.equals(this.status);
    }

    public boolean isReady() {
        return ReservationStatus.READY.equals(this.status);
    }

    @Override
    public int compareTo(Reservation other) {
        if (other == null) return 1;
        // Priority by queue position, then reservation timestamp
        int posComp = Integer.compare(this.queuePosition, other.queuePosition);
        if (posComp != 0) return posComp;
        if (this.reservedAt != null && other.reservedAt != null) {
            return this.reservedAt.compareTo(other.reservedAt);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", memberId=" + memberId +
                ", queuePosition=" + queuePosition +
                ", status=" + status +
                '}';
    }
}
