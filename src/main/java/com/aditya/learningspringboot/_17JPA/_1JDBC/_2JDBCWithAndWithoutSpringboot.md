====================================================
JDBC WITHOUT SPRING BOOT (PLAIN JDBC)
====================================================

import java.sql.*;

public class UserDao {

    public void getUsers() {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            // 0. Driver class loading
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. DB connection making
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/testdb",
                "root",
                "password"
            );

            statement = connection.createStatement();
            rs = statement.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }

        } catch (ClassNotFoundException e) {
            // 2. Exception handling (Driver not found)
            e.printStackTrace();
        } catch (SQLException e) {
            // 2. Exception handling (DB related)
            e.printStackTrace();
        } finally {
            // 3. Closing DB resources (VERY IMPORTANT)
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

Problems:
- Boilerplate code
- Manual connection handling
- Error-prone resource closing
- No connection pooling
- Poor performance under load

====================================================
JDBC WITH SPRING BOOT – JdbcTemplate
====================================================

Spring Boot internally handles:
- Driver loading
- Connection pool
- Exception translation
- Resource cleanup

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> getUsers() {
        return jdbcTemplate.query(
            "SELECT name FROM users",
            (rs, rowNum) -> rs.getString("name")
        );
    }
}

Benefits:
- No DriverManager usage
- No try-catch-finally
- Uses connection pool
- SQLException → DataAccessException (unchecked)

====================================================
WHAT IS CONNECTION POOL?
====================================================

Connection Pool:
- A cache of reusable DB connections
- Avoids creating new connection for each request
- Improves performance & scalability

Why needed?
- Creating DB connections is expensive
- High traffic apps need fast reuse

Flow:
Client → Pool → Connection → DB
↑
Returned back

====================================================
WHAT IS HIKARICP?
====================================================

HikariCP:
- High-performance JDBC connection pool
- Default pool in Spring Boot
- Extremely fast & lightweight

Features:
- Low latency
- Auto timeout handling
- Leak detection
- Thread-safe
- Production-grade

====================================================
DRIVERMANAGER – DEBUGGING ROLE
====================================================

DriverManager:
- Legacy JDBC connection provider
- Used mainly for debugging or standalone apps

Example:
DriverManager.getDrivers()
DriverManager.getLoginTimeout()

Spring Boot DOES NOT use DriverManager directly in runtime.
It uses DataSource (HikariCP internally).

====================================================
THEORY CHECKPOINTS (IMPORTANT)
====================================================

0. Driver Class Loading
----------------------
Old Way:
Class.forName("com.mysql.cj.jdbc.Driver");

Modern Way:
- JDBC 4+
- Driver auto-loaded via META-INF/services
- Spring Boot handles automatically

1. DB Connection Making
----------------------
Old:
DriverManager.getConnection()

Modern:
DataSource.getConnection()
(Connection fetched from pool)

2. Exception Handling
--------------------
Old:
Checked SQLException everywhere

Spring:
SQLException → DataAccessException
(Unchecked, consistent, portable)

3. Closing DB Resources
----------------------
Old:
Manual close in finally block

Spring:
Automatically handled by JdbcTemplate

4. Manual Connection Pool Handling (OLD WAY)
---------------------------------------------
- Apache DBCP
- C3P0
- Custom pools (NOT recommended)

Modern:
HikariCP (default & best)

====================================================
HIKARI DATASOURCE CONFIGURATION
====================================================

application.yml

spring:
datasource:
url: jdbc:mysql://localhost:3306/testdb
username: root
password: password
driver-class-name: com.mysql.cj.jdbc.Driver

    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      connection-timeout: 20000
      max-lifetime: 1800000
      pool-name: HikariPool-1

Explanation:
- minimum-idle → Minimum idle connections
- maximum-pool-size → Max concurrent DB connections
- connection-timeout → Wait time for a connection
- max-lifetime → Prevents stale connections

====================================================
SUMMARY (INTERVIEW GOLD)
====================================================

- ORM removes JDBC boilerplate
- JdbcTemplate simplifies JDBC access
- Connection pooling is mandatory for performance
- HikariCP is the fastest and default pool
- Spring Boot manages driver, pool, exceptions, and cleanup
- NEVER use DriverManager directly in Spring apps
  ====================================================
