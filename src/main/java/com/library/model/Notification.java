package com.library.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long memberId;
    private String title;
    private String message;
    private NotificationType type = NotificationType.GENERAL;
    private boolean read = false;
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(Long id, Long memberId, String title, String message, NotificationType type, boolean read) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
    }

    public Notification(Long memberId, String title, String message, NotificationType type) {
        this.memberId = memberId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", read=" + read +
                '}';
    }
}
