package com.library.service;

import com.library.validation.LibraryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FineServiceTest {

    private FineService fineService;

    @BeforeEach
    public void setUp() {
        fineService = new FineService();
    }

    @Test
    @DisplayName("Zero fine when returned on the due date")
    public void testZeroFineOnDueDate() {
        LocalDate due = LocalDate.now();
        LocalDate returned = LocalDate.now();

        BigDecimal fine = fineService.calculateFine(due, returned);
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    @DisplayName("Zero fine when returned before the due date")
    public void testZeroFineBeforeDueDate() {
        LocalDate due = LocalDate.now().plusDays(5);
        LocalDate returned = LocalDate.now();

        BigDecimal fine = fineService.calculateFine(due, returned);
        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    @DisplayName("Accurate fine calculation for overdue days using BigDecimal")
    public void testFineCalculationOverdue() {
        LocalDate due = LocalDate.now().minusDays(4);
        LocalDate returned = LocalDate.now();

        BigDecimal expected = LibraryPolicy.DAILY_FINE_RATE.multiply(BigDecimal.valueOf(4)); // 4 * 2.50 = 10.00
        BigDecimal fine = fineService.calculateFine(due, returned);

        assertEquals(0, expected.compareTo(fine));
        assertEquals(new BigDecimal("10.00"), fine);
    }

    @Test
    @DisplayName("Handles null dates gracefully")
    public void testNullDates() {
        assertEquals(BigDecimal.ZERO, fineService.calculateFine(null, LocalDate.now()));
        assertEquals(BigDecimal.ZERO, fineService.calculateFine(LocalDate.now(), null));
    }
}
