package com.library;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Embedded Tomcat Launcher for zero-configuration execution during demonstrations and viva evaluation.
 * Runs on http://localhost:8080/library
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portProp = System.getProperty("server.port");
        if (portProp != null) {
            try {
                port = Integer.parseInt(portProp);
            } catch (NumberFormatException ignored) {}
        }

        String webappDirLocation = "src/main/webapp/";
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Trigger HTTP connector initialization

        File baseDir = new File("target/tomcat-embed");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/library", new File(webappDirLocation).getAbsolutePath());
        ctx.setReloadable(true);
        // Ensure webapp parent class loader uses the runtime classpath
        ctx.setParentClassLoader(Main.class.getClassLoader());

        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        logger.info("============================================================================");
        logger.info(" Online Library Management System starting on: http://localhost:{}/library", port);
        logger.info(" Default Librarian: admin@library.local   Password: Admin@123");
        logger.info(" Default Member:    john.smith@library.local Password: Member@123");
        logger.info("============================================================================");

        tomcat.start();
        tomcat.getServer().await();
    }
}
