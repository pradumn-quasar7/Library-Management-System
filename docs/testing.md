# Testing Strategy & Verification Report
## Online Library Management System

---

## 1. Testing Strategy & Philosophy

The system's testing strategy emphasizes **deterministic unit testing of core business logic** and **strict transaction rollback verification** without depending on external container runtimes:

1. **Unit Testing (JUnit 5 & Mockito):**
   - Precise validation logic (ISBN-10, ISBN-13 check digits, Email RFC patterns, password strength rules).
   - Mathematical precision of fine accrual using `BigDecimal` and boundary day transitions.
2. **Transactional Integration Testing:**
   - Multi-step ACID atomic boundary testing for Borrow and Return workflows.
   - Verifying that exceptions thrown mid-transaction trigger clean rollbacks, leaving catalog counts, copy statuses, and audit records completely unmodified.
3. **End-to-End Functional Verification:**
   - Real-browser automated testing verifying UI flows, login authentication, session cookies, and dynamic form submissions.

---

## 2. Automated Test Suite Breakdown

### 2.1 Fine Calculation Tests (`FineServiceTest.java`)
Validates that library fine rules strictly conform to policy specifications:
- **No Fine for On-Time Returns:** Due date in future or returned before due date results in zero fine.
- **Grace Period Boundary:** Same-day return on due date produces $0.00 fine.
- **Overdue Day Accumulation:** Return 3 days late at $0.50/day results in exactly $1.50.
- **Extreme Overdue Ceiling:** Validates maximum fine capping (if configured) or exact precision across multi-week delinquency.
- **Monetary Precision:** Verifies scale of 2 decimal places with `RoundingMode.HALF_UP`.

### 2.2 Domain Validator Tests (`ValidationTest.java`)
Covers edge cases across input validators:
- **ISBNValidator:**
  - Valid ISBN-10: `0-306-40615-2` (Pass)
  - Valid ISBN-13: `978-0-13-468599-1`, `978-0-13-235088-4` (Pass)
  - Invalid Check Digit: `978-0-13-468599-9` (Fails with `ValidationException`)
  - Malformed length: `12345` (Fails)
- **EmailValidator:**
  - Standard email: `patron@university.edu` (Pass)
  - Missing `@` or domain: `patron.com` (Fails)
- **PasswordValidator:**
  - Strong password with upper, lower, digit, special symbol: `Lib@2026Secure` (Pass)
  - Weak password under 8 characters: `lib1` (Fails)
  - Missing special character: `Password123` (Fails)

### 2.3 ACID Transaction Tests (`BorrowReturnTransactionTest.java`)
Tests the transactional integrity of `LoanService`:
- **Successful Atomic Borrow:**
  1. Copy status transitions `AVAILABLE` -> `BORROWED`.
  2. `books.available_copies` decrements from $N$ to $N - 1$.
  3. Active `Loan` record created with due date $= \text{now} + 14 \text{ days}$.
  4. `AuditLog` entry created recording the transaction.
- **Failure & Rollback Verification:**
  - Simulating an unexpected failure (e.g. audit write failure or quota violation).
  - Confirms database state rolls back: copy remains `AVAILABLE`, catalog available copies count remains unchanged, and no orphaned loan record exists.

---

## 3. Automated Test Execution Results

Command executed:
```bash
mvn test
```

### Output Summary:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.library.service.FineServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.124 s -- in com.library.service.FineServiceTest
[INFO] Running com.library.validation.ValidationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.042 s -- in com.library.validation.ValidationTest
[INFO] Running com.library.service.BorrowReturnTransactionTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.318 s -- in com.library.service.BorrowReturnTransactionTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 4. Manual Verification Scenarios

| Test Case | Scenario | Expected Outcome | Status |
| :--- | :--- | :--- | :--- |
| **TC-01** | Unauthenticated user visits `/librarian/dashboard` | Redirected to `/auth/login` | **PASS** |
| **TC-02** | Member session attempts `/librarian/books/add` | Returns HTTP 403 Forbidden page (`/WEB-INF/views/error/403.jsp`) | **PASS** |
| **TC-03** | Librarian logs in with `admin@library.local` / `Admin@123` | Redirects to `/librarian/dashboard` with active KPI stats | **PASS** |
| **TC-04** | Librarian downloads CSV report `/librarian/reports/export?type=inventory` | Browser downloads `inventory.csv` formatted with RFC-4180 headers | **PASS** |
| **TC-05** | Member reserves a title with 0 available copies | Ticket created with status `PENDING` | **PASS** |
| **TC-06** | Reading room booking collision | Submitting booking for already booked seat & slot returns validation error | **PASS** |
