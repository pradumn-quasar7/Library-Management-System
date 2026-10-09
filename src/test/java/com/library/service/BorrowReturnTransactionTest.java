package com.library.service;

import com.library.dao.BookCopyDAO;
import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.dao.impl.BookCopyDAOImpl;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.LoanDAOImpl;
import com.library.dao.impl.MemberDAOImpl;
import com.library.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowReturnTransactionTest {

    private final LoanService loanService = new LoanService();
    private final BookDAO bookDAO = new BookDAOImpl();
    private final BookCopyDAO bookCopyDAO = new BookCopyDAOImpl();
    private final MemberDAO memberDAO = new MemberDAOImpl();
    private final LoanDAO loanDAO = new LoanDAOImpl();

    @Test
    @DisplayName("End-to-End Atomic Transaction: Borrow and Return Book")
    public void testBorrowAndReturnWorkflow() {
        // Find member Emily Davis (id: 2)
        Member member = memberDAO.findById(2L);
        assertNotNull(member, "Member Emily Davis should exist in database");

        // Find available book 'Modern Operating Systems' (id: 6)
        Book book = bookDAO.findById(6L);
        assertNotNull(book, "Book should exist");
        int initialAvailable = book.getAvailableCopies();
        assertTrue(initialAvailable > 0, "Book should have at least 1 available copy");

        // Execute Borrow Transaction
        Loan loan = loanService.borrowBook(member.getId(), book.getId());
        assertNotNull(loan, "Loan should be created");
        assertNotNull(loan.getId(), "Loan ID should be generated");
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());

        // Verify copy status is now BORROWED
        BookCopy copy = bookCopyDAO.findById(loan.getCopyId());
        assertNotNull(copy);
        assertEquals(CopyStatus.BORROWED, copy.getStatus());

        // Verify available count decreased by 1
        Book bookAfterBorrow = bookDAO.findById(6L);
        assertEquals(initialAvailable - 1, bookAfterBorrow.getAvailableCopies());

        // Execute Return Transaction
        Fine fine = loanService.returnBook(loan.getId(), 1L); // Returned on time, no fine
        assertNull(fine, "No fine should be assessed for returning on time");

        // Verify loan status is RETURNED
        Loan returnedLoan = loanDAO.findById(loan.getId());
        assertEquals(LoanStatus.RETURNED, returnedLoan.getStatus());
        assertNotNull(returnedLoan.getReturnedAt());

        // Verify copy status restored to AVAILABLE
        BookCopy copyAfterReturn = bookCopyDAO.findById(loan.getCopyId());
        assertEquals(CopyStatus.AVAILABLE, copyAfterReturn.getStatus());

        // Verify book available count restored
        Book bookAfterReturn = bookDAO.findById(6L);
        assertEquals(initialAvailable, bookAfterReturn.getAvailableCopies());
    }
}
