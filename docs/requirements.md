# Software Requirements Specification (SRS)
## Online Library Management System

**Document Version:** 1.0.0  
**Target Platform:** Java 21 LTS, Jakarta Servlets 6.0, Jakarta Server Pages 3.1, MySQL 8.0/9.0  
**Architecture:** Layered Model-View-Controller (MVC) without Framework Abstractions  

---

## 1. Introduction

### 1.1 Purpose
This document specifies the software requirements for the enterprise-grade Online Library Management System. The system provides end-to-end automated cataloging, borrowing, reservation queues, fine assessment, quiet reading room seat bookings, automated notifications, algorithmic book recommendations, and audit logging.

### 1.2 Scope
The system addresses the operational workflows of academic and public libraries, supporting two primary user roles: **Librarians** (system administrators and catalog managers) and **Members** (students, faculty, and community patrons). The implementation adheres strictly to standard Java Enterprise specifications (Jakarta EE 10) and standard relational database constraints.

### 1.3 Key Definitions & Acronyms
- **LTS:** Long-Term Support
- **MVC:** Model-View-Controller architectural pattern
- **RBAC:** Role-Based Access Control
- **ISBN:** International Standard Book Number (10 or 13 digits)
- **DAO:** Data Access Object
- **FIFO:** First-In, First-Out (reservation queuing discipline)

---

## 2. Overall Description

### 2.1 User Roles & Actor Matrix

| Actor | Description | Key Responsibilities | Access Boundaries |
| :--- | :--- | :--- | :--- |
| **Librarian / Admin** | Privileged administrator responsible for physical catalog and member accounts | Catalog maintenance, inventory copy tracking, loan issuance/returns, reading room seat creation, audit log inspection, fine collection, CSV exports | Full access to `/librarian/*` routes and management servlets |
| **Member** | Authenticated library patron (Student/Faculty/Community) | Catalog search/filtering, personal loan renewals, reservations, fine payments, seat bookings, notifications, viewing personalized recommendations | Restricted to `/member/*` routes; blocked from administrative servlets |
| **Anonymous Visitor** | Unauthenticated user | Landing page overview, catalog browsing, registration, authentication | Public endpoints (`/`, `/catalog`, `/auth/*`) |

### 2.2 Core Operating Constraints
1. **Zero External Frameworks:** No Spring Boot, Hibernate, Spring Security, or JS UI libraries (React/Angular/Vue).
2. **Standard Jakarta EE APIs:** Jakarta Servlets 6.0 (`jakarta.servlet.*`), JSP 3.1 (`jakarta.servlet.jsp.*`), and JSTL 3.0 (`jakarta.servlet.jsp.jstl.*`).
3. **Acidic Transactions:** Strict multi-step ACID guarantees with manual `Connection` auto-commit management, row-level locking (`SELECT ... FOR UPDATE`), and rollback handlers.
4. **Data Integrity:** Database-enforced foreign keys, unique composite indexes, and optimistic/pessimistic copy tracking.

---

## 3. Functional Requirements

### 3.1 Authentication & Session Security (AUTH)
- **REQ-AUTH-01:** The system shall authenticate users using email and BCrypt-hashed passwords (cost factor 10).
- **REQ-AUTH-02:** User accounts shall possess an active/suspended status flag; suspended members cannot authenticate or borrow items.
- **REQ-AUTH-03:** The system shall establish an HTTP session containing the authenticated `User` domain object and role claim.
- **REQ-AUTH-04:** Passwords must meet minimum complexity requirements (>= 8 characters, uppercase, lowercase, digit, and special symbol).
- **REQ-AUTH-05:** Unauthenticated attempts to access protected routes must redirect to `/auth/login` preserving the original requested path.

### 3.2 Book Catalog & Inventory Management (CAT)
- **REQ-CAT-01:** Librarians can create, view, update, and soft-delete/deactivate catalog titles with ISBN, title, author, category, publisher, publication year, shelf location, and synopsis.
- **REQ-CAT-02:** The system must enforce unique ISBN validation (supporting both ISBN-10 and ISBN-13 formats with check-digit validation).
- **REQ-CAT-03:** Each catalog title can have multiple physical inventory copies (`BookCopy`), each tracked by a distinct barcode and physical condition (`NEW`, `GOOD`, `FAIR`, `DAMAGED`, `LOST`).
- **REQ-CAT-04:** The system shall support real-time full-text search across Title, Author, ISBN, and Category with pagination and availability filters.

### 3.3 Circulation & Transactional Borrowing (CIR)
- **REQ-CIR-01:** Librarians can issue books to members by scanning/entering copy barcodes and member IDs.
- **REQ-CIR-02:** The system must reject borrow requests if:
  - The member has reached their maximum quota (default: 5 books for regular students).
  - The member has unpaid overdue fines exceeding $10.00.
  - The member has existing overdue loans.
  - The specific copy is not in `AVAILABLE` status.
- **REQ-CIR-03:** The borrow transaction must atomically create a `Loan` record, update `BookCopy` status to `BORROWED`, decrement available catalog count, and log an `AuditLog` entry in a single ACID transaction.
- **REQ-CIR-04:** The return transaction must atomically record return timestamp, update copy condition, calculate any accrued overdue fines, set copy status to `AVAILABLE` (or `RESERVED` if a pending reservation exists), and increment catalog counts.
- **REQ-CIR-05:** Members may renew active loans once, provided the book has no pending reservation queue.

### 3.4 Reservation Queuing (RES)
- **REQ-RES-01:** Members can place reservations on catalog titles that currently have 0 available copies.
- **REQ-RES-02:** Reservations are prioritized using a strict FIFO queue based on reservation timestamp.
- **REQ-RES-03:** When a borrowed copy is returned, the system automatically fulfills the top reservation queue item, transitions its status to `READY_FOR_PICKUP`, reserves the copy, and triggers an alert notification with a 48-hour pickup expiration.
- **REQ-RES-04:** Expired reservations that are not collected within 48 hours are automatically cancelled by the background scheduler, liberating the copy for the next member in queue.

### 3.5 Fine Assessment & Payment Management (FIN)
- **REQ-FIN-01:** Overdue loans accrue fines at a rate of $0.50 per calendar day beyond the due date.
- **REQ-FIN-02:** Fines are calculated using Java `BigDecimal` arithmetic with `RoundingMode.HALF_UP` to ensure monetary precision.
- **REQ-FIN-03:** Members can view itemized fine breakdowns and initiate simulated online payments.
- **REQ-FIN-04:** Librarians can manually waive or record cash settlement of fines with mandatory audit logging.

### 3.6 Reading Room & Study Seat Allocation (ROOM)
- **REQ-ROOM-01:** The library manages numbered reading room desks categorized by amenities (Power Socket, Dual Monitors, Quiet Zone, Window View).
- **REQ-ROOM-02:** Members can book seats in time blocks (Morning 09:00-13:00, Afternoon 13:00-17:00, Evening 17:00-21:00).
- **REQ-ROOM-03:** The system strictly prevents overlapping bookings on the same seat for identical dates and slots using database unique keys and transactional validation.

### 3.7 Algorithmic Recommendation Engine (REC)
- **REQ-REC-01:** The system provides an explainable book recommendation feed for authenticated members.
- **REQ-REC-02:** The scoring algorithm evaluates three signals:
  1. Category affinity from personal loan history (weight: 40%).
  2. Author affinity from personal loan history (weight: 30%).
  3. High-demand popular titles across all library circulation (weight: 30%).
- **REQ-REC-03:** Each recommendation displays a transparent explanation string (e.g., *"Recommended because you borrowed 3 books in Computer Science"*).

### 3.8 Background Daemons & Notifications (NOTIF)
- **REQ-NOTIF-01:** A multi-threaded `NotificationScheduler` executes daemon tasks:
  1. Hourly check for loans due in <= 24 hours (generates courtesy reminders).
  2. Nightly assessment of overdue loans and fine incrementation.
  3. Sweep for expired reservations (exceeding 48 hours pickup window).
- **REQ-NOTIF-02:** Members can view unread in-app alerts and dismiss/mark them as read.

### 3.9 Reporting & Data Export (REP)
- **REQ-REP-01:** Librarians have access to analytical dashboard metrics: Active Loans, Overdue Books, Outstanding Fines, Total Inventory, and Popular Categories.
- **REQ-REP-02:** The system streams standard RFC-4180 CSV exports for:
  - Full Inventory Catalog (`inventory.csv`).
  - Active and Historical Circulation Loans (`loans.csv`).
  - Outstanding and Settled Fines (`fines.csv`).

---

## 4. Non-Functional Requirements

### 4.1 Performance & Concurrency
- **NFR-PERF-01:** Database connection pooling handled via HikariCP configured with 10 max pool connections and 250ms connection timeout.
- **NFR-PERF-02:** Database read queries under 50ms through optimized compound indexes (`idx_loans_member_status`, `idx_books_category_isbn`).
- **NFR-PERF-03:** Page rendering latency under 100ms on standard local runtime.

### 4.2 Security & Integrity
- **NFR-SEC-01:** SQL Injection prevention: 100% of dynamic database queries use parameterized `PreparedStatement` interfaces.
- **NFR-SEC-02:** Cross-Site Scripting (XSS) prevention: JSTL `<c:out>` and manual HTML entity escaping for all user-supplied input rendered in JSPs.
- **NFR-SEC-03:** CSRF mitigation: Post-Redirect-Get pattern implemented across all write actions.
- **NFR-SEC-04:** Passwords hashed with BCrypt and salt factor 10. Plaintext passwords never stored or logged.

### 4.3 Reliability & Availability
- **NFR-REL-01:** Graceful servlet context shutdown: `AppContextListener` shuts down `ScheduledExecutorService` with 5-second graceful await termination.
- **NFR-REL-02:** Connection leaks prevented by strict `try-with-resources` or standardized `ConnectionManager.closeQuietly()` in `finally` blocks.

### 4.4 Usability & Aesthetics
- **NFR-UX-01:** Modern enterprise glassmorphic SaaS interface built with custom responsive Vanilla CSS (CSS Variables, Flexbox, CSS Grid).
- **NFR-UX-02:** Zero browser console errors; mobile-responsive layout for desktop, tablet, and smartphone viewports.
