-- ============================================================================
-- Online Library Management System - Seed Data
-- ============================================================================

USE library_db;

-- Clear any existing data in reverse foreign key order
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reading_room_bookings;
TRUNCATE TABLE reading_room_seats;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE notification_preferences;
TRUNCATE TABLE notifications;
TRUNCATE TABLE fines;
TRUNCATE TABLE reservations;
TRUNCATE TABLE loans;
TRUNCATE TABLE book_copies;
TRUNCATE TABLE book_authors;
TRUNCATE TABLE books;
TRUNCATE TABLE genres;
TRUNCATE TABLE authors;
TRUNCATE TABLE members;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- 1. USERS
-- Default passwords:
-- Admin:  'Admin@123'  -> $2a$10$DTb9wxklvSfUvhLhBNNUQ.4.dYV8Zt6nX2hGa7U2iQQSIEmP1koA2
-- Member: 'Member@123' -> $2a$10$HeGWOqOYA3EwsFpQoWsrJe4mpNoc6Q9bEIfN9EtraeeWnrFvPssxK
-- ----------------------------------------------------------------------------
INSERT INTO users (id, email, password_hash, role, status) VALUES
(1, 'admin@library.local', '$2a$10$DTb9wxklvSfUvhLhBNNUQ.4.dYV8Zt6nX2hGa7U2iQQSIEmP1koA2', 'LIBRARIAN', 'ACTIVE'),
(2, 'john.smith@library.local', '$2a$10$HeGWOqOYA3EwsFpQoWsrJe4mpNoc6Q9bEIfN9EtraeeWnrFvPssxK', 'MEMBER', 'ACTIVE'),
(3, 'emily.davis@library.local', '$2a$10$HeGWOqOYA3EwsFpQoWsrJe4mpNoc6Q9bEIfN9EtraeeWnrFvPssxK', 'MEMBER', 'ACTIVE'),
(4, 'michael.brown@library.local', '$2a$10$HeGWOqOYA3EwsFpQoWsrJe4mpNoc6Q9bEIfN9EtraeeWnrFvPssxK', 'MEMBER', 'ACTIVE'),
(5, 'sarah.wilson@library.local', '$2a$10$HeGWOqOYA3EwsFpQoWsrJe4mpNoc6Q9bEIfN9EtraeeWnrFvPssxK', 'MEMBER', 'ACTIVE');

-- ----------------------------------------------------------------------------
-- 2. MEMBERS
-- ----------------------------------------------------------------------------
INSERT INTO members (id, user_id, membership_id, full_name, phone, address, joined_at) VALUES
(1, 2, 'LIB-MEM-1001', 'John Smith', '+1-555-0101', '124 Elm Street, Springfield', '2026-01-10'),
(2, 3, 'LIB-MEM-1002', 'Emily Davis', '+1-555-0102', '458 Oak Lane, Metropolis', '2026-02-15'),
(3, 4, 'LIB-MEM-1003', 'Michael Brown', '+1-555-0103', '789 Pine Ave, Gotham', '2026-03-01'),
(4, 5, 'LIB-MEM-1004', 'Sarah Wilson', '+1-555-0104', '321 Maple Blvd, Star City', '2026-03-12');

-- ----------------------------------------------------------------------------
-- 3. AUTHORS
-- ----------------------------------------------------------------------------
INSERT INTO authors (id, name, biography) VALUES
(1, 'Joshua Bloch', 'Chief Java Architect and author of Effective Java.'),
(2, 'Robert C. Martin', 'Software craftsman, author of Clean Code and Clean Architecture.'),
(3, 'Martin Fowler', 'Chief Scientist at ThoughtWorks, expert on enterprise application patterns.'),
(4, 'Brian Goetz', 'Java Language Architect at Oracle, author of Java Concurrency in Practice.'),
(5, 'Thomas H. Cormen', 'Co-author of Introduction to Algorithms (CLRS).'),
(6, 'Andrew S. Tanenbaum', 'Professor of Computer Science, author of Modern Operating Systems.'),
(7, 'Donald E. Knuth', 'Legendary computer scientist, author of The Art of Computer Programming.'),
(8, 'George Orwell', 'English novelist, essayist, and critic known for 1984 and Animal Farm.'),
(9, 'Stephen Hawking', 'Theoretical physicist and cosmologist, author of A Brief History of Time.'),
(10, 'Eric Ries', 'Entrepreneur and pioneer of the Lean Startup movement.');

-- ----------------------------------------------------------------------------
-- 4. GENRES
-- ----------------------------------------------------------------------------
INSERT INTO genres (id, name, description) VALUES
(1, 'Computer Science', 'Foundational computational theory, algorithms, and architectures.'),
(2, 'Software Engineering', 'Practices, patterns, clean code, and software development methodologies.'),
(3, 'Data Science & AI', 'Machine learning, artificial intelligence, statistics, and big data.'),
(4, 'Fiction & Literature', 'Classic, modern, and speculative fiction literature.'),
(5, 'Science & Nature', 'Physics, astronomy, biology, and scientific exploration.'),
(6, 'Business & Leadership', 'Entrepreneurship, management, strategy, and business innovation.'),
(7, 'Operating Systems & Networks', 'System design, operating systems, and network communications.');

-- ----------------------------------------------------------------------------
-- 5. BOOKS
-- ----------------------------------------------------------------------------
INSERT INTO books (id, title, isbn, publisher, publication_year, description, genre_id, status) VALUES
(1, 'Effective Java (3rd Edition)', '978-0134685991', 'Addison-Wesley', 2018, 'The definitive guide to Java platform best practices by Joshua Bloch.', 2, 'ACTIVE'),
(2, 'Clean Code: A Handbook of Agile Software Craftsmanship', '978-0132350884', 'Prentice Hall', 2008, 'A must-read handbook for writing clean, readable, and maintainable software.', 2, 'ACTIVE'),
(3, 'Java Concurrency in Practice', '978-0321349606', 'Addison-Wesley', 2006, 'The gold standard authority on multithreaded and concurrent Java programming.', 2, 'ACTIVE'),
(4, 'Patterns of Enterprise Application Architecture', '978-0321127426', 'Addison-Wesley', 2002, 'Enterprise architectural patterns for robust transactional systems.', 2, 'ACTIVE'),
(5, 'Introduction to Algorithms (4th Edition)', '978-0262046305', 'MIT Press', 2022, 'Comprehensive textbook on algorithms and data structures commonly known as CLRS.', 1, 'ACTIVE'),
(6, 'Modern Operating Systems (4th Edition)', '978-0133591620', 'Pearson', 2014, 'Detailed exploration of operating systems concepts, memory, and concurrency.', 7, 'ACTIVE'),
(7, 'The Art of Computer Programming, Vol 1', '978-0201896831', 'Addison-Wesley', 1997, 'Donald Knuth landmark series on fundamental algorithms.', 1, 'ACTIVE'),
(8, '1984', '978-0451524935', 'Signet Classic', 1949, 'Dystopian social science fiction novel and cautionary tale about totalitarianism.', 4, 'ACTIVE'),
(9, 'A Brief History of Time', '978-0553380163', 'Bantam Books', 1988, 'Landmark exploration of cosmology, black holes, and the origin of the universe.', 5, 'ACTIVE'),
(10, 'The Lean Startup', '978-0307887894', 'Crown Business', 2011, 'How todays entrepreneurs use continuous innovation to create radically successful businesses.', 6, 'ACTIVE');

-- ----------------------------------------------------------------------------
-- 6. BOOK AUTHORS
-- ----------------------------------------------------------------------------
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), -- Effective Java -> Joshua Bloch
(2, 2), -- Clean Code -> Robert C. Martin
(3, 4), -- Java Concurrency -> Brian Goetz
(4, 3), -- Enterprise Architecture -> Martin Fowler
(5, 5), -- Intro to Algorithms -> Thomas Cormen
(6, 6), -- Modern OS -> Andrew Tanenbaum
(7, 7), -- TAOCP -> Donald Knuth
(8, 8), -- 1984 -> George Orwell
(9, 9), -- Brief History -> Stephen Hawking
(10, 10); -- Lean Startup -> Eric Ries

-- ----------------------------------------------------------------------------
-- 7. BOOK COPIES (Physical Inventory)
-- ----------------------------------------------------------------------------
INSERT INTO book_copies (id, book_id, accession_number, status, condition_notes) VALUES
-- Effective Java (3 copies: 2 Available, 1 Borrowed)
(1, 1, 'EJ-001-C1', 'AVAILABLE', 'New copy, excellent condition'),
(2, 1, 'EJ-001-C2', 'AVAILABLE', 'Clean copy, shelf A1'),
(3, 1, 'EJ-001-C3', 'BORROWED', 'Borrowed by John Smith'),

-- Clean Code (3 copies: 1 Available, 1 Borrowed, 1 Reserved)
(4, 2, 'CC-002-C1', 'AVAILABLE', 'Clean copy, shelf A2'),
(5, 2, 'CC-002-C2', 'BORROWED', 'Borrowed by Emily Davis'),
(6, 2, 'CC-002-C3', 'RESERVED', 'Reserved for pending pickup'),

-- Java Concurrency in Practice (2 copies: 1 Available, 1 Borrowed)
(7, 3, 'JCP-003-C1', 'AVAILABLE', 'Shelf A3'),
(8, 3, 'JCP-003-C2', 'BORROWED', 'Borrowed by Michael Brown'),

-- Patterns of Enterprise Application Architecture (2 copies: 2 Available)
(9, 4, 'PEAA-004-C1', 'AVAILABLE', 'Shelf A4'),
(10, 4, 'PEAA-004-C2', 'AVAILABLE', 'Shelf A4'),

-- Introduction to Algorithms (3 copies: 1 Available, 1 Borrowed - Overdue, 1 Maintenance)
(11, 5, 'CLRS-005-C1', 'AVAILABLE', 'Reference Section'),
(12, 5, 'CLRS-005-C2', 'BORROWED', 'Overdue copy with Sarah Wilson'),
(13, 5, 'CLRS-005-C3', 'MAINTENANCE', 'Spine repair in progress'),

-- Modern Operating Systems (2 copies: 2 Available)
(14, 6, 'MOS-006-C1', 'AVAILABLE', 'Shelf B1'),
(15, 6, 'MOS-006-C2', 'AVAILABLE', 'Shelf B1'),

-- The Art of Computer Programming (2 copies: 2 Available)
(16, 7, 'TAOCP-007-C1', 'AVAILABLE', 'Rare collection shelf'),
(17, 7, 'TAOCP-007-C2', 'AVAILABLE', 'Rare collection shelf'),

-- 1984 (2 copies: 1 Available, 1 Borrowed)
(18, 8, '1984-008-C1', 'AVAILABLE', 'Fiction Section F1'),
(19, 8, '1984-008-C2', 'BORROWED', 'Borrowed by John Smith'),

-- A Brief History of Time (2 copies: 2 Available)
(20, 9, 'BHT-009-C1', 'AVAILABLE', 'Science Section S1'),
(21, 9, 'BHT-009-C2', 'AVAILABLE', 'Science Section S1'),

-- The Lean Startup (2 copies: 2 Available)
(22, 10, 'TLS-010-C1', 'AVAILABLE', 'Business Section M1'),
(23, 10, 'TLS-010-C2', 'AVAILABLE', 'Business Section M1');

-- ----------------------------------------------------------------------------
-- 8. LOANS (Active & Past Loans)
-- ----------------------------------------------------------------------------
INSERT INTO loans (id, copy_id, member_id, borrowed_at, due_date, returned_at, renewal_count, status) VALUES
-- Loan 1: John Smith borrowed Effective Java (Active, due in 7 days)
(1, 3, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), NULL, 0, 'ACTIVE'),

-- Loan 2: Emily Davis borrowed Clean Code (Active, due tomorrow)
(2, 5, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), NULL, 1, 'ACTIVE'),

-- Loan 3: Michael Brown borrowed Java Concurrency (Active, due in 10 days)
(3, 8, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), NULL, 0, 'ACTIVE'),

-- Loan 4: Sarah Wilson borrowed CLRS (OVERDUE by 5 days)
(4, 12, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 19 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), NULL, 0, 'OVERDUE'),

-- Loan 5: John Smith borrowed 1984 (Active, due in 12 days)
(5, 19, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 12 DAY), NULL, 0, 'ACTIVE'),

-- Loan 6: Completed Historical Loan (Returned on time)
(6, 1, 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 40 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 26 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 28 DAY), 0, 'RETURNED');

-- ----------------------------------------------------------------------------
-- 9. RESERVATIONS
-- ----------------------------------------------------------------------------
INSERT INTO reservations (id, book_id, member_id, reserved_at, queue_position, status, expires_at) VALUES
-- Clean Code has a reservation by Michael Brown (Ready for pickup)
(1, 2, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 1, 'READY', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 24 HOUR)),
-- Clean Code has second reservation by Sarah Wilson (Waiting in queue)
(2, 2, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 2, 'WAITING', NULL);

-- ----------------------------------------------------------------------------
-- 10. FINES
-- ----------------------------------------------------------------------------
INSERT INTO fines (id, loan_id, member_id, amount, reason, status, paid_at, created_at) VALUES
-- Sarah Wilson has an unpaid fine for overdue CLRS (5 days * $2.50 = $12.50)
(1, 4, 4, 12.50, 'Overdue return penalty (5 days late at $2.50/day)', 'UNPAID', NULL, CURRENT_TIMESTAMP),
-- Historical paid fine for John Smith
(2, 6, 1, 5.00, 'Late return fee (2 days overdue)', 'PAID', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 28 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 28 DAY));

-- ----------------------------------------------------------------------------
-- 11. NOTIFICATIONS
-- ----------------------------------------------------------------------------
INSERT INTO notifications (id, member_id, title, message, type, is_read, created_at) VALUES
(1, 2, 'Book Due Soon: Clean Code', 'Your borrowed book "Clean Code" is due tomorrow. Please return or renew it to avoid overdue fines.', 'DUE_SOON', FALSE, CURRENT_TIMESTAMP),
(2, 4, 'Overdue Alert: Introduction to Algorithms', 'Your loan for "Introduction to Algorithms" is 5 days overdue. Accumulated fine is $12.50.', 'OVERDUE', FALSE, CURRENT_TIMESTAMP),
(3, 3, 'Reservation Ready for Pickup', 'Your reserved title "Clean Code" is ready at the circulation desk. Hold expires in 24 hours.', 'RESERVATION_READY', FALSE, CURRENT_TIMESTAMP),
(4, 1, 'Welcome to Online Library', 'Your membership is active! Explore our catalog and reserve reading room seats anytime.', 'GENERAL', TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 60 DAY));

-- ----------------------------------------------------------------------------
-- 12. NOTIFICATION PREFERENCES
-- ----------------------------------------------------------------------------
INSERT INTO notification_preferences (member_id, due_date_alert, overdue_alert, new_arrival_alert, reservation_alert) VALUES
(1, TRUE, TRUE, TRUE, TRUE),
(2, TRUE, TRUE, TRUE, TRUE),
(3, TRUE, TRUE, TRUE, TRUE),
(4, TRUE, TRUE, TRUE, TRUE);

-- ----------------------------------------------------------------------------
-- 13. AUDIT LOGS
-- ----------------------------------------------------------------------------
INSERT INTO audit_logs (actor_user_id, action, entity_type, entity_id, description, created_at) VALUES
(1, 'SYSTEM_INITIALIZATION', 'SYSTEM', 1, 'Database seeded with default catalog and members', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 60 DAY)),
(1, 'BOOK_CREATED', 'BOOK', 1, 'Added book title: Effective Java (3rd Edition)', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 50 DAY)),
(1, 'MEMBER_REGISTERED', 'MEMBER', 1, 'Registered new member: John Smith (LIB-MEM-1001)', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 45 DAY)),
(1, 'LOAN_ISSUED', 'LOAN', 1, 'Issued copy EJ-001-C3 to John Smith', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
(1, 'FINE_ASSESSED', 'FINE', 1, 'Assessed fine $12.50 for overdue loan #4', CURRENT_TIMESTAMP);

-- ----------------------------------------------------------------------------
-- 14. READING ROOM SEATS
-- ----------------------------------------------------------------------------
INSERT INTO reading_room_seats (id, seat_number, status) VALUES
(1, 'SEAT-A01', 'AVAILABLE'),
(2, 'SEAT-A02', 'AVAILABLE'),
(3, 'SEAT-A03', 'AVAILABLE'),
(4, 'SEAT-A04', 'AVAILABLE'),
(5, 'SEAT-A05', 'AVAILABLE'),
(6, 'SEAT-A06', 'AVAILABLE'),
(7, 'SEAT-B01', 'AVAILABLE'),
(8, 'SEAT-B02', 'AVAILABLE'),
(9, 'SEAT-B03', 'AVAILABLE'),
(10, 'SEAT-B04', 'AVAILABLE'),
(11, 'SEAT-C01', 'MAINTENANCE'),
(12, 'SEAT-C02', 'AVAILABLE');

-- ----------------------------------------------------------------------------
-- 15. READING ROOM BOOKINGS
-- ----------------------------------------------------------------------------
INSERT INTO reading_room_bookings (id, member_id, seat_id, booking_date, start_time, end_time, status) VALUES
(1, 1, 1, CURRENT_DATE, '09:00:00', '12:00:00', 'BOOKED'),
(2, 2, 2, CURRENT_DATE, '13:00:00', '16:00:00', 'BOOKED'),
(3, 3, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00', '14:00:00', 'BOOKED');
