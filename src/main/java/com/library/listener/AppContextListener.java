package com.library.listener;

import com.library.scheduler.NotificationScheduler;
import com.library.util.ConnectionManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Online Library Management System web application initializing...");
        try {
            // Test connection pool initialization
            ConnectionManager.getConnection().close();
            logger.info("Database connection verified successfully.");

            // Start background scheduler
            NotificationScheduler.start();
            logger.info("Online Library Management System initialized successfully.");
        } catch (Exception e) {
            logger.error("Error during application startup", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Online Library Management System shutting down...");
        try {
            NotificationScheduler.stop();
            ConnectionManager.closePool();
            logger.info("Application shutdown completed cleanly.");
        } catch (Exception e) {
            logger.error("Error during application shutdown", e);
        }
    }
}
