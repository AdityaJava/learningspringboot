========================================
HikariCP, HikariDataSource & Connection Pool — IMPORTANT THEORY
========================================

1) What is a Database Connection?
---------------------------------
- A database connection is a TCP connection between the application and the database.
- Creating a connection is EXPENSIVE:
    - Network handshake
    - Authentication
    - Resource allocation on DB server
- Creating & closing a connection for every request is very slow and unsafe at scale.

----------------------------------------

2) What is a Connection Pool?
-----------------------------
- A connection pool is a set of pre-created, reusable database connections.
- Connections are created once at application startup.
- Requests BORROW a connection and RETURN it after use.

Flow WITHOUT pool:
Request → Open Connection → Execute SQL → Close Connection

Flow WITH pool:
Request → Borrow Connection → Execute SQL → Return Connection to Pool

Benefits:
- High performance
- Reduced latency
- Controlled DB load
- Better scalability

----------------------------------------

3) What is HikariCP?
-------------------
- HikariCP is a high-performance JDBC connection pool.
- Default connection pool in Spring Boot (2.x and 3.x).
- Lightweight, fast, and production-grade.
- Manages lifecycle, reuse, and limits of DB connections.

Key idea:
HikariCP = "engine" that manages the pool.

----------------------------------------

4) What is HikariDataSource?
---------------------------
- DataSource is a Java interface that provides database connections.
- HikariDataSource is HikariCP’s implementation of DataSource.
- Spring Boot uses HikariDataSource internally.

Important:
- Application NEVER creates DB connections directly.
- It always calls:
  DataSource.getConnection()
- HikariDataSource returns a pooled connection.

----------------------------------------

5) Relationship (VERY IMPORTANT)
--------------------------------
HikariCP
|
└── HikariDataSource (implements DataSource)
|
└── Connection Pool
|
├── Connection 1
├── Connection 2
├── Connection 3

----------------------------------------

6) How Spring Boot Uses HikariCP
--------------------------------
- Reads DB config from application.properties / application.yml
- Creates HikariDataSource bean
- Initializes connection pool at startup
- Repositories use the pool automatically

Controller → Service → Repository
↓
HikariDataSource
↓
Pooled DB Connection

----------------------------------------

7) Pool Size & Protection
-------------------------
- Pool has MAX connections (maximum-pool-size).
- If all connections are in use:
    - New request waits
    - If wait exceeds connection-timeout → Exception thrown
- This protects the database from overload.

----------------------------------------

8) Common Interview Answers
---------------------------
Q: What is HikariCP?
A: HikariCP is a high-performance JDBC connection pool used by Spring Boot to efficiently manage and reuse database connections.

Q: What is HikariDataSource?
A: HikariDataSource is the DataSource implementation provided by HikariCP that applications use to obtain pooled database connections.

Q: Why connection pooling?
A: To avoid expensive connection creation per request, improve performance, and control database load.

----------------------------------------

9) One-Line Summary
-------------------
- Connection Pool = Reusable DB connections
- HikariCP = Pool implementation
- HikariDataSource = Entry point to the pool
- Spring Boot uses HikariCP by default

========================================
