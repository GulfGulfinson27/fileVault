#!/bin/bash

# Standardport, falls keiner angegeben wird
PORT=${1:-9090}
mvn exec:java -Dexec.mainClass="com.filevault.api.FileVaultApiApp" -Dexec.args="$PORT" -Dexec.cleanupDaemonThreads=false -q