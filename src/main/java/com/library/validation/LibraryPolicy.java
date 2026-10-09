package com.library.validation;

import java.math.BigDecimal;

public final class LibraryPolicy {

    public static final int MAX_ACTIVE_LOANS = 5;
    public static final int DEFAULT_LOAN_DAYS = 14;
    public static final int MAX_RENEWALS = 2;
    public static final BigDecimal DAILY_FINE_RATE = new BigDecimal("2.50");
    public static final int RESERVATION_EXPIRY_HOURS = 48;
    public static final int MAX_DAILY_READING_ROOM_BOOKINGS = 2;

    private LibraryPolicy() {
        // Enforce non-instantiability
    }
}
