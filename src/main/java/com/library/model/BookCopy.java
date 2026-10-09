package com.library.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class BookCopy implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bookId;
    private String accessionNumber;
    private CopyStatus status;
    private String conditionNotes;
    private Timestamp createdAt;

    // Transient display fields
    private String bookTitle;
    private String bookIsbn;

    public BookCopy() {
    }

    public BookCopy(Long id, Long bookId, String accessionNumber, CopyStatus status, String conditionNotes) {
        this.id = id;
        this.bookId = bookId;
        this.accessionNumber = accessionNumber;
        this.status = status;
        this.conditionNotes = conditionNotes;
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

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public String getConditionNotes() {
        return conditionNotes;
    }

    public void setConditionNotes(String conditionNotes) {
        this.conditionNotes = conditionNotes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public boolean isAvailable() {
        return CopyStatus.AVAILABLE.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCopy copy = (BookCopy) o;
        return Objects.equals(id, copy.id) && Objects.equals(accessionNumber, copy.accessionNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accessionNumber);
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "id=" + id +
                ", accessionNumber='" + accessionNumber + '\'' +
                ", status=" + status +
                '}';
    }
}
