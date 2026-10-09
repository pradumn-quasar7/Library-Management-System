#!/bin/bash
# docker-entrypoint.sh
# Waits for MySQL to be ready, then starts Tomcat.
# Railway injects: DB_URL, DB_USER, DB_PASSWORD, MYSQL_HOST, MYSQL_PORT

set -e

MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

echo "Waiting for MySQL at $MYSQL_HOST:$MYSQL_PORT ..."
until nc -z "$MYSQL_HOST" "$MYSQL_PORT" 2>/dev/null; do
  echo "  MySQL not ready yet — retrying in 2s..."
  sleep 2
done
echo "MySQL is up!"

# Execute CMD (catalina.sh run)
exec "$@"
