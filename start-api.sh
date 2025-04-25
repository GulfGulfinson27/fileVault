#!/bin/bash

# Standardport, falls keiner angegeben wird
PORT=${1:-8080}

# Wechsel in das Projektverzeichnis und starte den Server mit Maven
cd /home/pschneid/Documents/fileVault || exit
mvn exec:java -Dexec.mainClass="com.filevault.api.FileVaultApiApp" -Dexec.args="$PORT" -Dexec.cleanupDaemonThreads=false -q