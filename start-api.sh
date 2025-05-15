#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME ist nicht gesetzt. Bitte setzen Sie JAVA_HOME auf Ihr JDK-Verzeichnis."
    exit 1
fi

JAVAFX_LIB="lib/javafx-sdk-17.0.14/lib"
JAVAFX_MODULES="javafx.controls,javafx.fxml"

if [ ! -d "$JAVAFX_LIB" ]; then
    echo "JavaFX-Bibliotheken nicht gefunden in $JAVAFX_LIB"
    exit 1
fi

echo "Starte FileVault API..."
"$JAVA_HOME/bin/java" \
    --module-path "$JAVAFX_LIB" \
    --add-modules "$JAVAFX_MODULES" \
    -jar target/FileVault-shaded.jar --api-only

if [ $? -ne 0 ]; then
    echo "Fehler beim Starten der API."
    exit 1
fi