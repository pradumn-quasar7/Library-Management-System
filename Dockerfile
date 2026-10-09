# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy dependency descriptors first for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build WAR (skip tests — tests need a live DB)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM tomcat:10.1-jdk21

LABEL maintainer="pradumn-quasar7"
LABEL description="Library Management System — Java 21 + Tomcat 10.1 + MySQL"

# Remove default Tomcat web apps to keep the image clean
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
           /usr/local/tomcat/webapps/examples \
           /usr/local/tomcat/webapps/docs \
           /usr/local/tomcat/webapps/host-manager \
           /usr/local/tomcat/webapps/manager

# Deploy the application WAR as ROOT so it's served at /
COPY --from=builder /build/target/library-management.war /usr/local/tomcat/webapps/ROOT.war

# Run the DB schema init script on first startup via an entrypoint wrapper
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["catalina.sh", "run"]
