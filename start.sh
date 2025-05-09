#!/bin/bash

# Default API port
PORT=${1:-9090}

# Start the FileVault application with API server
mvn exec:java -Dexec.mainClass="com.filevault.FileVaultApp" -Dexec.args="--api-port=$PORT" -Dexec.cleanupDaemonThreads=false

read -p "Press [Enter] to continue..."