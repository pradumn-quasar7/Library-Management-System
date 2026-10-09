# Relational Database Design & Schema Specification
## Online Library Management System

---

## 1. Database Architecture & Schema Overview

- **Database Engine:** MySQL 8.0+ / 9.0+ InnoDB
- **Character Set:** `utf8mb4` (collation: `utf8mb4_unicode_ci`)
- **Isolation Level:** Read Committed / Repeatable Read
- **Transactions:** Fully ACID compliant with foreign key constraints (`ON DELETE RESTRICT` / `CASCADE`) and B-Tree indexing.

### Schema Entity Relationship Diagram (ERD Summary)

```
[users] 1 --- 1 [members]
   |                 |
   |                 +--- M [loans] M --- 1 [book_copies] 1 --- 1 [books]
   |                 |        |
   |                 |        +--- 1 [fines]
   |                 |
   |                 +--- M [reservations] M --- 1 [books]
   |                 |
   |                 +--- M [reading_room_bookings] M --- 1 [reading_room_seats]
   |                 |
   |                 +--- M [notifications]
   |
   +--- M [audit_logs]
```

---

## 2. Table Specifications & Data Dictionary

### 2.1 Core Authentication & Members

#### `users`
Master credential table.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `email` | VARCHAR(120) | NO | UNIQUE | Login email |
| `password_hash` | VARCHAR(255) | NO | | BCrypt hash (salt factor 10) |
| `role` | VARCHAR(20) | NO | CHECK (role IN ('LIBRARIAN', 'MEMBER')) | Role enum |
| `is_active` | BOOLEAN | NO | DEFAULT TRUE | Account state |
| `created_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Registration date |
| `updated_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update date |

#### `members`
Extended profile for library patrons.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `user_id` | BIGINT | NO | UNIQUE, FK -> users(id) ON DELETE CASCADE | Linked user account |
| `first_name` | VARCHAR(60) | NO | | Given name |
| `last_name` | VARCHAR(60) | NO | | Family name |
| `phone` | VARCHAR(20) | YES | | Contact telephone |
| `membership_number`| VARCHAR(30)| NO | UNIQUE | Barcode/Card ID (e.g., MEM-0001) |
| `membership_type` | VARCHAR(20) | NO | CHECK ('STUDENT', 'FACULTY', 'COMMUNITY')| Quota category |
| `max_books_allowed`| INT | NO | DEFAULT 5 | Active borrow quota |
| `status` | VARCHAR(20) | NO | DEFAULT 'ACTIVE' | Membership status |
| `created_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

---

### 2.2 Book Catalog & Physical Inventory

#### `books`
Canonical catalog metadata.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Title identifier |
| `isbn` | VARCHAR(20) | NO | UNIQUE | ISBN-10 or ISBN-13 |
| `title` | VARCHAR(255) | NO | | Work title |
| `author` | VARCHAR(150) | NO | | Primary author(s) |
| `publisher` | VARCHAR(120) | YES | | Publishing house |
| `publication_year` | INT | YES | | Year of printing |
| `category` | VARCHAR(80) | NO | | Genre / Subject classification |
| `shelf_location` | VARCHAR(50) | YES | | Physical rack / bay (e.g. CS-A1-04) |
| `total_copies` | INT | NO | DEFAULT 1 | Total inventory count |
| `available_copies`| INT | NO | DEFAULT 1 | Readily borrowable count |
| `description` | TEXT | YES | | Abstract / Synopsis |
| `created_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Catalog entry date |

#### `book_copies`
Individual physical item tracking.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Item instance ID |
| `book_id` | BIGINT | NO | FK -> books(id) ON DELETE CASCADE | Parent catalog work |
| `barcode` | VARCHAR(50) | NO | UNIQUE | Physical scanner barcode |
| `status` | VARCHAR(20) | NO | CHECK ('AVAILABLE', 'BORROWED', 'RESERVED', 'LOST', 'DAMAGED') | Item circulation state |
| `condition_status` | VARCHAR(20) | NO | DEFAULT 'GOOD' | Physical quality |
| `created_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Registration date |

---

### 2.3 Circulation, Loans & Reservations

#### `loans`
Borrowing transactions and due date records.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Circulation ID |
| `book_copy_id` | BIGINT | NO | FK -> book_copies(id) | Issued physical copy |
| `member_id` | BIGINT | NO | FK -> members(id) | Borrower patron |
| `loan_date` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Checkout timestamp |
| `due_date` | TIMESTAMP | NO | | Target return deadline (14 days) |
| `returned_at` | TIMESTAMP | YES | | Actual check-in timestamp |
| `renewal_count` | INT | NO | DEFAULT 0 | Renewals consumed (max: 1) |
| `status` | VARCHAR(20) | NO | CHECK ('ACTIVE', 'RETURNED', 'OVERDUE', 'LOST') | Circulation status |

#### `reservations`
FIFO title waiting list.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Queue ticket ID |
| `book_id` | BIGINT | NO | FK -> books(id) ON DELETE CASCADE | Target catalog title |
| `member_id` | BIGINT | NO | FK -> members(id) | Queued patron |
| `reserved_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Queue priority timestamp |
| `expires_at` | TIMESTAMP | YES | | Pickup collection deadline (48h) |
| `status` | VARCHAR(20) | NO | CHECK ('PENDING', 'READY_FOR_PICKUP', 'FULFILLED', 'CANCELLED', 'EXPIRED') | Reservation stage |

---

### 2.4 Fines, Study Seats & Supporting Entities

#### `fines`
Financial penalties assessed for late returns.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Fine identifier |
| `loan_id` | BIGINT | NO | FK -> loans(id) ON DELETE CASCADE | Linked circulation record |
| `amount` | DECIMAL(8,2)| NO | | Assessed amount in USD |
| `paid_amount` | DECIMAL(8,2)| NO | DEFAULT 0.00 | Settled balance |
| `status` | VARCHAR(20) | NO | CHECK ('PENDING', 'PAID', 'WAIVED') | Settlement state |
| `assessed_at` | TIMESTAMP | NO | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `settled_at` | TIMESTAMP | YES | | Payment/Waiver timestamp |

#### `reading_room_seats`
Library study workspace catalog.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Desk identifier |
| `seat_number` | VARCHAR(20) | NO | UNIQUE | Number label (e.g., SEAT-101) |
| `has_power` | BOOLEAN | NO | DEFAULT TRUE | AC electrical outlet flag |
| `has_monitor` | BOOLEAN | NO | DEFAULT FALSE | External display monitor flag |
| `is_quiet_zone` | BOOLEAN | NO | DEFAULT TRUE | Silent study wing flag |
| `is_active` | BOOLEAN | NO | DEFAULT TRUE | Desk availability |

#### `reading_room_bookings`
Seat reservation allocations.

| Column | Type | Nullable | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | NO | PRIMARY KEY, AUTO_INCREMENT | Booking identifier |
| `seat_id` | BIGINT | NO | FK -> reading_room_seats(id) | Allocated seat |
| `member_id` | BIGINT | NO | FK -> members(id) | Booked patron |
| `booking_date`| DATE | NO | | Day of booking |
| `time_slot` | VARCHAR(20) | NO | CHECK ('MORNING', 'AFTERNOON', 'EVENING') | Daily slot |
| `status` | VARCHAR(20) | NO | DEFAULT 'CONFIRMED' | Booking status |

---

## 3. Database Indexes for High-Velocity Circulation

To prevent full-table scans across high-volume queries, the following targeted composite and single-column B-Tree indexes are deployed in `database/indexes.sql`:

1. **`idx_loans_member_status`** on `loans(member_id, status)`:
   - Powers member dashboard active borrowings query and quota enforcement.
2. **`idx_loans_due_status`** on `loans(due_date, status)`:
   - Powers the scheduled daemon overdue assessment queries.
3. **`idx_reservations_book_status`** on `reservations(book_id, status, reserved_at)`:
   - Enables constant-time FIFO head retrieval (`ORDER BY reserved_at ASC LIMIT 1`).
4. **`idx_books_category_isbn`** on `books(category, isbn)`:
   - Speeds catalog category browsing and recommendation scoring.
5. **`idx_fines_member_status`** on `fines(loan_id, status)`:
   - Accelerates outstanding balance calculations during borrow validation.
6. **`idx_reading_room_seat_slot`** on `reading_room_bookings(seat_id, booking_date, time_slot)`:
   - Guarantees immediate conflict detection for desk bookings.
