#!/bin/bash
DIR=$(dirname "$0")
"$DIR/java/bin/java" -jar "$DIR/cms-server-7.8.0.jar" "$@"