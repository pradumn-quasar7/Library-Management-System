#!/bin/bash
# docker-entrypoint.sh
# Sets Tomcat to listen on Railway's $PORT, then starts Tomcat.
# HikariCP handles DB connection retries automatically.

set -e

TOMCAT_PORT="${PORT:-8080}"

# Patch Tomcat's HTTP connector port to match Railway's assigned $PORT
sed -i "s/port=\"8080\"/port=\"${TOMCAT_PORT}\"/g" /usr/local/tomcat/conf/server.xml
echo "Tomcat configured to listen on port ${TOMCAT_PORT}"

# Execute CMD (catalina.sh run)
exec "$@"
