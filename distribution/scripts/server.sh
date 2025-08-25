#!/bin/bash
DIR=$(dirname "$0")
"$DIR/java/bin/java" -jar "$DIR/cms-server-@CMS_VERSION@.jar" "$@"