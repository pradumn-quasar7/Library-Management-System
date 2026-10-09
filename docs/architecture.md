# System Architecture & Technical Design
## Online Library Management System

---

## 1. Architectural Overview

The application is structured according to the classic **Layered Model-View-Controller (MVC)** architectural pattern, adhering strictly to **Jakarta EE 10 standards** without third-party frameworks.

```
+-------------------------------------------------------------------------+
|                        Presentation Tier (View)                         |
|   Jakarta Server Pages 3.1 (JSP) + JSTL 3.0 Core + SaaS Vanilla CSS     |
+-------------------------------------------------------------------------+
                                    |
                                    v (HTTP Requests / Form Submissions)
+-------------------------------------------------------------------------+
|                        Security & Filter Chain                          |
|  1. CharacterEncodingFilter  -->  UTF-8 normalization                   |
|  2. AuthenticationFilter     -->  Protected path inspection             |
|  3. Role Authorization       -->  LibrarianFilter & MemberFilter        |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                        Controller Tier (Servlets)                       |
|   Jakarta Servlets 6.0 (@WebServlet) handling REST-like URI routing,    |
|   session extraction, parameter validation, and DTO dispatch            |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                        Service Tier (Business Logic)                    |
|   - LoanService (ACID Borrow/Return)      - AuthService (BCrypt)        |
|   - ReservationService (FIFO Queues)      - FineService (BigDecimal)    |
|   - RecommendationService (Affinity)      - ReadingRoomService (Seats)  |
|   - ReportService (CSV Generators)        - NotificationService (Alerts)|
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                        Data Access Tier (DAO Pattern)                   |
|   - Clean DAO Interfaces in `com.library.dao`                           |
|   - JDBC 4.3 implementations in `com.library.dao.impl`                  |
|   - HikariCP Connection Pooling (`com.library.dao.ConnectionManager`)   |
|   - Manual Transaction Boundaries (`setAutoCommit(false)` / `rollback`) |
+-------------------------------------------------------------------------+
                                    |
                                    v (SQL Queries / Row Locks)
+-------------------------------------------------------------------------+
|                        Relational Database Tier                         |
|   MySQL 8.0/9.0 InnoDB Engine with Foreign Keys & B-Tree Indexes        |
+-------------------------------------------------------------------------+
```

---

## 2. Layer Responsibilities & Design Patterns

### 2.1 Presentation Tier (View)
- **Technology:** JSP 3.1 with JSTL 3.0 (`jakarta.tags.core`).
- **Template Modularity:** Structured header, navbar, flash messages banner (`messages.jsp`), and footer partials embedded via `<jsp:include>`.
- **Styling Architecture:** Modern SaaS styling implemented strictly in Vanilla CSS without Tailwind or Bootstrap:
  - `main.css`: Global design tokens, color palette (deep indigo, slate, emerald, crimson), typography, buttons, tables, badges.
  - `dashboard.css`: Analytical KPI cards, statistical grids, chart-like visual bars.
  - `forms.css`: Polished form groups, floating labels, validation states.
  - `responsive.css`: Breakpoints for 1200px, 992px, 768px, and 480px.

### 2.2 Security & Interception (Filter Chain)
The application secures routes using a pipeline of Jakarta EE `HttpFilter` components configured in `web.xml`:

```
Request ---> [CharacterEncodingFilter]
                   | (Forces UTF-8 request/response)
                   v
             [AuthenticationFilter]
                   | (Checks session for valid User object)
                   +---> No session & protected URL? -> Redirect to /auth/login
                   v
             [LibrarianAuthorizationFilter]   OR   [MemberAuthorizationFilter]
                   | (/librarian/*)                         | (/member/*)
                   +-> Role != LIBRARIAN? -> 403 Forbidden  +-> Role != MEMBER? -> 403 Forbidden
                   v
             [Target Controller Servlet]
```

### 2.3 Controller Tier (Servlets)
- Annotated with `@WebServlet(urlPatterns = { ... })`.
- Implements HTTP verb routing (`doGet` for idempotent queries/rendering, `doPost` for state mutations).
- Enforces the **Post-Redirect-Get (PRG)** pattern to eliminate duplicate submissions on browser refresh.
- Passes feedback through flash attributes stored in `HttpSession` (`flashMessage` and `flashType`), consumed once and cleared by the presentation layer.

### 2.4 Service Tier (Business Domain Logic)
- Encapsulates pure business rules, validation, mathematical calculations, and transaction orchestration.
- Zero servlet or HTTP dependencies in this layer—classes can be unit tested in complete isolation.
- Key Transaction Flow in `LoanService.borrowBook()`:
  ```java
  Connection conn = ConnectionManager.getConnection();
  try {
      conn.setAutoCommit(false); // Begin ACID boundary
      
      // 1. Pessimistic lock copy: SELECT * FROM book_copies WHERE id = ? FOR UPDATE
      BookCopy copy = bookCopyDao.findByIdForUpdate(conn, copyId);
      if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
          throw new BookNotAvailableException("Book copy is currently unavailable");
      }
      
      // 2. Validate member eligibility (active, < quota, fines < threshold)
      validateMemberEligibility(conn, memberId);
      
      // 3. Create Loan record
      Loan loan = new Loan(...);
      loanDao.create(conn, loan);
      
      // 4. Update Copy status to BORROWED
      bookCopyDao.updateStatus(conn, copyId, BookCopyStatus.BORROWED);
      
      // 5. Decrement book available count
      bookDao.decrementAvailableCopies(conn, copy.getBookId());
      
      // 6. Write Audit Log
      auditLogDao.create(conn, new AuditLog(...));
      
      conn.commit(); // Commit all changes atomically
  } catch (Exception ex) {
      ConnectionManager.rollbackQuietly(conn); // Roll back on ANY failure
      throw ex;
  } finally {
      ConnectionManager.closeQuietly(conn); // Return connection to Hikari pool
  }
  ```

### 2.5 Data Access Tier (DAO & HikariCP)
- Follows the DAO Interface / JDBC Implementation separation.
- `ConnectionManager` manages a singleton `HikariDataSource` configured from `db.properties`.
- Resource cleanup is strictly guaranteed via `ConnectionManager.closeQuietly(...)`.

---

## 3. Multithreading & Background Daemons

A dedicated background daemon service (`NotificationScheduler`) runs within the web container, managed by `AppContextListener`.

```
[AppContextListener.contextInitialized]
            |
            v
Creates ScheduledExecutorService (Executors.newScheduledThreadPool(2, threadFactory))
            |
            +---> Task 1: Due Date Reminder (Initial: 1 min, Period: 1 hour)
            |     Scans loans due within 24 hours, creates in-app notifications.
            |
            +---> Task 2: Overdue Fine Assessor (Initial: 5 min, Period: 24 hours)
            |     Calculates daily $0.50 fines on overdue loans and issues alerts.
            |
            +---> Task 3: Reservation Expiration Sweeper (Initial: 2 min, Period: 1 hour)
                  Cancels expired pickup reservations (>48 hrs) and promotes next in queue.
```

### Graceful Lifecycle Management
When the servlet container undeploys or stops:
1. `AppContextListener.contextDestroyed(ServletContextEvent sce)` is invoked.
2. `NotificationScheduler.shutdown()` calls `scheduler.shutdown()`.
3. Blocks up to 5 seconds with `scheduler.awaitTermination(5, TimeUnit.SECONDS)`.
4. Closes the HikariCP datasource to prevent MySQL connection leaks and daemon orphan threads.

---

## 4. Algorithmic Recommendation Engine Design

The recommendation engine in `RecommendationService` implements an explainable content-affinity scoring model:

$$\text{Score}(B) = 0.40 \cdot \text{Affinity}_{\text{category}}(B) + 0.30 \cdot \text{Affinity}_{\text{author}}(B) + 0.30 \cdot \text{Popularity}(B)$$

1. **Category Affinity (40%):** Counts frequency of books borrowed by the member in book $B$'s category.
2. **Author Affinity (30%):** Counts frequency of books borrowed by the member by book $B$'s author.
3. **Global Popularity (30%):** Overall circulation velocity of title $B$ across all members.
4. **Transparency/Explainability:** Constructs human-readable rationale:
   - *"Top recommendation: You read 4 books in Artificial Intelligence."*
   - *"Popular choice: Borrowed 18 times by library members."*
