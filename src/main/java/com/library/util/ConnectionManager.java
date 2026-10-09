package com.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private static HikariDataSource dataSource;

    static {
        initDataSource();
    }

    private ConnectionManager() {
        // Enforce non-instantiability
    }

    private static synchronized void initDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            Properties props = new Properties();
            try (InputStream in = ConnectionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    logger.info("Loaded db.properties successfully.");
                } else {
                    logger.warn("db.properties not found in classpath. Falling back to default settings.");
                }
            }

            HikariConfig config = new HikariConfig();
            // Resolution order: env var → system property → db.properties → default
            String url      = resolveConfig("DB_URL",      "DB_URL",      props, "db.url",      "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
            String username = resolveConfig("DB_USER",     "DB_USER",     props, "db.username",  "root");
            String password = resolveConfig("DB_PASSWORD", "DB_PASSWORD", props, "db.password",  "");
            String driver   = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(driver);

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "15")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "5")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "300000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "20000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1200000")));

            config.setPoolName("LibraryConnectionPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("HikariDataSource connection pool initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize HikariDataSource connection pool", e);
            throw new ExceptionInInitializerError("Database pool initialization failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariDataSource connection pool...");
            dataSource.close();
            logger.info("HikariDataSource connection pool closed.");
        }
    }

    public static void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                logger.debug("Transaction rolled back successfully.");
            } catch (SQLException e) {
                logger.error("Error during transaction rollback", e);
            }
        }
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    logger.debug("Error closing resource: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Resolves a configuration value using the following priority order:
     * 1. Environment variable (e.g., set by Railway / Docker)
     * 2. JVM system property (-D flag)
     * 3. Properties file value
     * 4. Hard-coded default
     */
    private static String resolveConfig(String envKey, String sysPropKey,
                                        Properties props, String propKey, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isBlank()) return envVal;
        String sysPropVal = System.getProperty(sysPropKey);
        if (sysPropVal != null && !sysPropVal.isBlank()) return sysPropVal;
        String propVal = props.getProperty(propKey);
        if (propVal != null && !propVal.isBlank()) return propVal;
        return defaultValue;
    }
}
