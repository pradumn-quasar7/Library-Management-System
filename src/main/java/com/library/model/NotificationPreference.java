package com.library.model;

import java.io.Serializable;
import java.util.Objects;

public class NotificationPreference implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private boolean dueDateAlert = true;
    private boolean overdueAlert = true;
    private boolean newArrivalAlert = true;
    private boolean reservationAlert = true;

    public NotificationPreference() {
    }

    public NotificationPreference(Long memberId, boolean dueDateAlert, boolean overdueAlert, boolean newArrivalAlert, boolean reservationAlert) {
        this.memberId = memberId;
        this.dueDateAlert = dueDateAlert;
        this.overdueAlert = overdueAlert;
        this.newArrivalAlert = newArrivalAlert;
        this.reservationAlert = reservationAlert;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public boolean isDueDateAlert() {
        return dueDateAlert;
    }

    public void setDueDateAlert(boolean dueDateAlert) {
        this.dueDateAlert = dueDateAlert;
    }

    public boolean isOverdueAlert() {
        return overdueAlert;
    }

    public void setOverdueAlert(boolean overdueAlert) {
        this.overdueAlert = overdueAlert;
    }

    public boolean isNewArrivalAlert() {
        return newArrivalAlert;
    }

    public void setNewArrivalAlert(boolean newArrivalAlert) {
        this.newArrivalAlert = newArrivalAlert;
    }

    public boolean isReservationAlert() {
        return reservationAlert;
    }

    public void setReservationAlert(boolean reservationAlert) {
        this.reservationAlert = reservationAlert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationPreference that = (NotificationPreference) o;
        return Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return "NotificationPreference{" +
                "memberId=" + memberId +
                ", dueDateAlert=" + dueDateAlert +
                ", overdueAlert=" + overdueAlert +
                '}';
    }
}
