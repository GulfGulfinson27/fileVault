# Basis-Image mit OpenJDK 17
FROM openjdk:17-jdk-slim

# Metadaten
LABEL org.opencontainers.image.source="https://github.com/GulfGulfinson/fileVault"
LABEL org.opencontainers.image.description="FileVault - Sichere Dateiverwaltung"
LABEL org.opencontainers.image.licenses=MIT

# Arbeitsverzeichnis setzen
WORKDIR /app

# Abhängigkeiten für JavaFX auf Debian
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libgtk-3-0 \
    libglib2.0-0 \
    libgl1 \
    libasound2 \
    libxtst6 \
    libx11-6 \
    libxxf86vm1 \
    locales \
    xorg \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Pfad für JavaFX erstellen
RUN mkdir -p /app/lib

# JavaFX SDK aus lokalem Projektverzeichnis kopieren
COPY lib/javafx-sdk-17.0.14 /app/lib/javafx-sdk-17.0.14

# JavaFX-Modulpfad setzen
ENV PATH_TO_FX=/app/lib/javafx-sdk-17.0.14/lib

# Kopiere die JAR-Datei
COPY target/FileVault-shaded.jar /app/FileVault.jar

# Port für die API freigeben
EXPOSE 9090

# Start-Befehl für die API mit JavaFX-Konfiguration
CMD ["sh", "-c", "Xvfb :99 -screen 0 1024x768x24 -nolisten tcp & DISPLAY=:99 java -Djava.awt.headless=true --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml -jar /app/FileVault.jar --api-port=9090"] 