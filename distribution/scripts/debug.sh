#!/bin/bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -z "$JAVA_OPTS" ]; then
  JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseZGC"
fi
JAVA_OPTS="$JAVA_OPTS --enable-native-access=ALL-UNNAMED -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=true -Djdk.lang.Process.launchMechanism=FORK"

exec "$DIR/java/bin/java" -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:8000 $JAVA_OPTS -jar "$DIR/cms-server-@CMS_VERSION@.jar" "$@"