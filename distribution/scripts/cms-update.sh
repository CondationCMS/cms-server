#!/bin/bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"

exec "$DIR/java/bin/java" ${JAVA_OPTS:-} -jar "$DIR/bin/cms-updater.jar" install --home "$DIR" "$@"
