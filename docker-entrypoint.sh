#!/bin/bash
# docker-entrypoint.sh
# Waits for MySQL to be ready using pure bash (no nc/netcat needed).
# Railway injects: DB_URL, DB_USER, DB_PASSWORD, MYSQL_HOST, MYSQL_PORT, PORT

set -e

MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
TOMCAT_PORT="${PORT:-8080}"
MAX_WAIT=120  # seconds
ELAPSED=0

# Update Tomcat connector port to match Railway's $PORT
sed -i "s/port=\"8080\"/port=\"${TOMCAT_PORT}\"/g" /usr/local/tomcat/conf/server.xml
echo "Tomcat configured to listen on port ${TOMCAT_PORT}"

echo "Waiting for MySQL at $MYSQL_HOST:$MYSQL_PORT ..."
until (echo > /dev/tcp/$MYSQL_HOST/$MYSQL_PORT) 2>/dev/null; do
  if [ "$ELAPSED" -ge "$MAX_WAIT" ]; then
    echo "Timed out waiting for MySQL after ${MAX_WAIT}s — starting Tomcat anyway"
    break
  fi
  echo "  MySQL not ready yet — retrying in 2s... (${ELAPSED}s elapsed)"
  sleep 2
  ELAPSED=$((ELAPSED + 2))
done

if (echo > /dev/tcp/$MYSQL_HOST/$MYSQL_PORT) 2>/dev/null; then
  echo "MySQL is up!"
fi

# Execute CMD (catalina.sh run)
exec "$@"
