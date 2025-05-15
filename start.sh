#!/bin/bash

# Default API port
PORT=${1:-9090}

# Prüfen, ob die JAR-Datei existiert
if [ -f "target/FileVault-shaded.jar" ]; then
    JAR_PATH="target/FileVault-shaded.jar"
elif [ -f "FileVault-shaded.jar" ]; then
    JAR_PATH="FileVault-shaded.jar"
elif [ -f "FileVault.jar" ]; then
    JAR_PATH="FileVault.jar"
else
    echo "Fehler: Keine FileVault JAR-Datei gefunden."
    exit 1
fi

# Start the FileVault application with API server
java -jar "$JAR_PATH" --api-port=$PORT

read -p "Drücken Sie [Enter], um fortzufahren..."