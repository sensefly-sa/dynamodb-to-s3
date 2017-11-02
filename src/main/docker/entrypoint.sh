#!/bin/sh
set -e
# https://unix.stackexchange.com/a/333870
set -f

java $JAVA_OPTS -jar /app.jar "$@"