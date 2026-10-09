# Controller Endpoints & Servlet Routing Reference
## Online Library Management System

---

## 1. Routing & Authorization Overview

Every request passes through the **Authentication and Role Authorization Filter pipeline**:
- **Public:** Accessible without active session.
- **Librarian Only (`ROLE_LIBRARIAN`):** Requires session `user.role == Role.LIBRARIAN`. Unauthorized access yields `HTTP 403 Forbidden`.
- **Member Only (`ROLE_MEMBER`):** Requires session `user.role == Role.MEMBER`. Unauthorized access yields `HTTP 403 Forbidden`.

---

## 2. Public & Authentication Endpoints

| URL Pattern | Servlet Class | HTTP Verbs | Access | Description & Query / Form Parameters |
| :--- | :--- | :--- | :--- | :--- |
| `/` | `HomeServlet` | `GET` | Public | Landing homepage with hero banner, quick search, featured books, and quick stats. |
| `/catalog` | `CatalogServlet` | `GET` | Public | Public catalog search. Params: `q` (search query), `category`, `availableOnly` (`true`/`false`), `page` (1-indexed). |
| `/auth/login` | `LoginServlet` | `GET`, `POST` | Public | **GET:** Renders login form.<br>**POST:** Authenticates credentials. Params: `email`, `password`. |
| `/auth/register` | `RegisterServlet` | `GET`, `POST` | Public | **GET:** Renders registration form.<br>**POST:** Creates new Member profile. Params: `firstName`, `lastName`, `email`, `phone`, `password`, `membershipType`. |
| `/auth/logout` | `LogoutServlet` | `GET`, `POST` | Authenticated | Invalidates current `HttpSession` and redirects to `/auth/login?logout=true`. |

---

## 3. Librarian Management Endpoints (`/librarian/*`)

| URL Pattern | Servlet Class | HTTP Verbs | Access | Description & Query / Form Parameters |
| :--- | :--- | :--- | :--- | :--- |
| `/librarian/dashboard` | `LibrarianDashboardServlet` | `GET` | Librarian | Analytical dashboard displaying KPI count metrics, recent circulations, overdue alerts, and category stats. |
| `/librarian/books` | `LibrarianBookListServlet` | `GET` | Librarian | Paginated management table of all catalog titles with inventory copy counts and status. |
| `/librarian/books/add` | `BookAddServlet` | `GET`, `POST` | Librarian | **GET:** Form to add a new book.<br>**POST:** Saves new title with initial copies. Params: `isbn`, `title`, `author`, `publisher`, `publicationYear`, `category`, `shelfLocation`, `initialCopies`, `description`. |
| `/librarian/books/edit` | `BookEditServlet` | `GET`, `POST` | Librarian | **GET:** Form to edit book info. Param: `id`.<br>**POST:** Updates title info. Params: `id`, `isbn`, `title`, `author`, etc. |
| `/librarian/books/delete` | `BookDeleteServlet` | `POST` | Librarian | Deactivates or removes a book title if no active loans exist. Param: `id`. |
| `/librarian/copies` | `BookCopyServlet` | `GET`, `POST` | Librarian | **GET:** Lists copies for book. Param: `bookId`.<br>**POST:** Adds new copy with unique barcode. Params: `bookId`, `barcode`, `condition`. |
| `/librarian/members` | `LibrarianMemberListServlet` | `GET` | Librarian | Searchable list of registered patrons, active loans count, and membership status. |
| `/librarian/issue` | `IssueBookServlet` | `GET`, `POST` | Librarian | **GET:** Issue form.<br>**POST:** Atomically issues copy to member. Params: `barcode`, `membershipNumber`. |
| `/librarian/return` | `ReturnBookServlet` | `GET`, `POST` | Librarian | **GET:** Return lookup form.<br>**POST:** Atomically checks in copy, assesses fine, and triggers reservation queue advancement. Params: `loanId` or `barcode`, `condition`. |
| `/librarian/fines` | `LibrarianFineServlet` | `GET`, `POST` | Librarian | **GET:** Lists outstanding and paid fines.<br>**POST:** Settles or waives fine. Params: `fineId`, `action` (`settle` or `waive`). |
| `/librarian/reports` | `ReportServlet` | `GET` | Librarian | Operational reporting page displaying circulation summaries and category breakdown. |
| `/librarian/reports/export` | `ReportExportServlet` | `GET` | Librarian | Streams downloadable RFC-4180 CSV file. Param: `type` (`inventory`, `loans`, or `fines`). Sets `Content-Disposition: attachment; filename=...`. |

---

## 4. Member Self-Service Endpoints (`/member/*`)

| URL Pattern | Servlet Class | HTTP Verbs | Access | Description & Query / Form Parameters |
| :--- | :--- | :--- | :--- | :--- |
| `/member/dashboard` | `MemberDashboardServlet` | `GET` | Member | Patron dashboard: active loans, due date warning banners, pending reservations, unread notifications, and top recommendations. |
| `/member/loans` | `MemberLoanServlet` | `GET`, `POST` | Member | **GET:** Lists current and past loans.<br>**POST:** Renews an active eligible loan. Param: `loanId`. |
| `/member/reservations`| `MemberReservationServlet` | `GET`, `POST` | Member | **GET:** Lists active reservation queue tickets.<br>**POST:** Places new reservation or cancels existing ticket. Params: `bookId` (to reserve), `reservationId` + `action=cancel`. |
| `/member/fines` | `MemberFineServlet` | `GET`, `POST` | Member | **GET:** Lists personal fine records.<br>**POST:** Simulates online fine payment. Param: `fineId`. |
| `/member/recommendations`| `MemberRecommendationServlet` | `GET` | Member | Algorithmic book suggestions with personal affinity and velocity explanation tags. |
| `/member/reading-room`| `ReadingRoomServlet` | `GET`, `POST` | Member | **GET:** Visual desk availability grid for selected date and slot. Params: `date`, `slot`.<br>**POST:** Books seat. Params: `seatId`, `date`, `slot`. |
| `/member/notifications`| `NotificationServlet` | `GET`, `POST` | Member | **GET:** View in-app notification inbox.<br>**POST:** Marks notification as read. Param: `id`. |
| `/member/profile` | `ProfileServlet` | `GET`, `POST` | Member | **GET:** Personal profile details.<br>**POST:** Updates contact phone number or password. |
