# Deployment & Configuration Guide
## Online Library Management System

---

## 1. Environment & Runtime Prerequisites

- **Operating System:** macOS, Linux (Ubuntu/Debian/RHEL), or Windows 10/11
- **Java Runtime Environment:** OpenJDK 21 LTS (or Oracle JDK 21)
- **Build Tool:** Apache Maven 3.9+
- **Relational Database:** MySQL Server 8.0+ or 9.0+
- **Application Server (Option A):** Standalone Apache Tomcat 10.1+ (Jakarta EE 10 compliant)
- **Application Server (Option B):** Embedded Tomcat Runner (bundled zero-config launcher via `com.library.Main`)

Verify runtime prerequisites:
```bash
java -version    # Must report Java 21
mvn -version     # Must report Maven 3.9+
mysql --version  # Must report MySQL 8.x or 9.x
```

---

## 2. Database Initialization

1. Connect to your local MySQL instance:
   ```bash
   mysql -u root -p
   ```

2. Execute the initialization scripts in sequence:
   ```sql
   -- 1. Create database and tables
   SOURCE /path/to/project/database/schema.sql;

   -- 2. Create optimized performance indexes
   SOURCE /path/to/project/database/indexes.sql;

   -- 3. Populate default catalog and test user accounts
   SOURCE /path/to/project/database/seed.sql;
   ```

3. Ensure `src/main/resources/db.properties` reflects your local MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
   db.user=root
   db.password=your_mysql_password
   db.pool.maxSize=10
   db.pool.minIdle=2
   db.pool.idleTimeout=30000
   db.pool.connectionTimeout=10000
   ```

---

## 3. Deployment Option A: Zero-Config Embedded Tomcat (Development & Evaluation)

The project includes an embedded Apache Tomcat launcher class (`com.library.Main`) that boots the compiled webapp directly from `src/main/webapp` and target classes without manual Tomcat installation.

### Step 1: Compile Classes
```bash
mvn compile
```

### Step 2: Launch Embedded Server
```bash
mvn exec:java -Dexec.mainClass="com.library.Main"
```

The server boots in ~1.5 seconds on port `8080`. Access the system in any web browser at:
```
http://localhost:8080/library
```

---

## 4. Deployment Option B: Production WAR File on Apache Tomcat 10

For traditional enterprise deployments on standalone Tomcat 10.1:

### Step 1: Package Production WAR
Run the Maven package goal:
```bash
mvn clean package
```
This generates the standardized production archive:
```
target/library-management.war
```

### Step 2: Deploy to Tomcat `webapps/`
1. Copy `target/library-management.war` to `$CATALINA_HOME/webapps/`:
   ```bash
   cp target/library-management.war /opt/tomcat/webapps/library.war
   ```
2. Start the Tomcat daemon:
   ```bash
   $CATALINA_HOME/bin/startup.sh
   ```
3. Tomcat will automatically explode the WAR file and initialize `AppContextListener`.
4. Access the application at:
   ```
   http://your-server-ip:8080/library
   ```

---

## 5. Seed Demonstration Accounts

The default seed database comes preloaded with the following verified test credentials:

| Role | Email | Password | Description |
| :--- | :--- | :--- | :--- |
| **Librarian (Admin)** | `admin@library.local` | `Admin@123` | Full access to catalog, circulation, fines, and reports. |
| **Member (Patron)** | `john.doe@example.com` | `Member@123` | Sample student patron with active loans and history. |
| **Member (Patron)** | `jane.smith@example.com` | `Member@123` | Sample faculty patron. |
