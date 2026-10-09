package com.library.dao;

import com.library.model.BookCopy;
import com.library.model.CopyStatus;

import java.sql.Connection;
import java.util.List;

public interface BookCopyDAO {
    BookCopy findById(Long id);
    BookCopy findById(Connection conn, Long id);
    BookCopy findByAccessionNumber(String accessionNumber);
    List<BookCopy> findCopiesByBookId(Long bookId);
    BookCopy findFirstAvailableCopy(Connection conn, Long bookId);
    Long create(Connection conn, BookCopy copy);
    boolean updateStatus(Connection conn, Long copyId, CopyStatus status);
    boolean updateCondition(Long copyId, String conditionNotes);
    int countCopiesByStatus(CopyStatus status);
    int countTotalCopies();
}
