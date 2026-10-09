-- ============================================================================
-- Online Library Management System - Database Indexes
-- ============================================================================

USE library_db;

-- Books & Catalog indexes
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_genre ON books(genre_id);
CREATE INDEX idx_books_status ON books(status);

-- Book Copies indexes
CREATE INDEX idx_copies_book ON book_copies(book_id);
CREATE INDEX idx_copies_status ON book_copies(status);
CREATE INDEX idx_copies_accession ON book_copies(accession_number);

-- Loans indexes
CREATE INDEX idx_loans_member ON loans(member_id);
CREATE INDEX idx_loans_copy ON loans(copy_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_due_date ON loans(due_date);

-- Reservations indexes
CREATE INDEX idx_reservations_book_status ON reservations(book_id, status);
CREATE INDEX idx_reservations_member ON reservations(member_id);
CREATE INDEX idx_reservations_queue ON reservations(book_id, queue_position);

-- Fines indexes
CREATE INDEX idx_fines_member ON fines(member_id);
CREATE INDEX idx_fines_status ON fines(status);

-- Notifications indexes
CREATE INDEX idx_notifications_member_read ON notifications(member_id, is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at);

-- Audit logs indexes
CREATE INDEX idx_audit_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- Reading room indexes
CREATE INDEX idx_rr_bookings_date ON reading_room_bookings(booking_date, seat_id);
CREATE INDEX idx_rr_bookings_member ON reading_room_bookings(member_id);
