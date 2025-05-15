#!/bin/bash

# Standardport, falls keiner angegeben wird
PORT=${1:-9090}

# Pfad zum JavaFX SDK festlegen
PATH_TO_FX="lib/javafx-sdk-17.0.14/lib"

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

# Prüfen, ob das JavaFX SDK vorhanden ist
if [ ! -d "$PATH_TO_FX" ]; then
    echo "Fehler: JavaFX SDK nicht gefunden in $PATH_TO_FX"
    exit 1
fi

# Starte nur die API ohne GUI
java -Djava.awt.headless=true --module-path "$PATH_TO_FX" --add-modules javafx.controls,javafx.fxml -jar "$JAR_PATH" --api-port=$PORT