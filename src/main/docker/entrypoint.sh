#!/bin/sh
set -e
# https://unix.stackexchange.com/a/333870
set -f

echo "Configuration:" && \
echo "  JAVA_OPTS:            $JAVA_OPTS" && \
echo "  CLOUDWATCH_LOG_GROUP: $CLOUDWATCH_LOG_GROUP" && \
echo "  RUN: java $JAVA_OPTS -jar /app.jar \"$@\"" && \
java $JAVA_OPTS -jar /app.jar "$@"