# Basis-Image mit OpenJDK 17 und JavaFX
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

# JavaFX SDK herunterladen und installieren (Linux version)
RUN apt-get update && apt-get install -y --no-install-recommends wget unzip && \
    mkdir -p /opt/javafx && \
    wget -q https://download2.gluonhq.com/openjfx/17.0.14/openjfx-17.0.14_linux-x64_bin-sdk.zip && \
    unzip openjfx-17.0.14_linux-x64_bin-sdk.zip -d /opt && \
    rm openjfx-17.0.14_linux-x64_bin-sdk.zip && \
    apt-get purge -y wget unzip && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# JavaFX-Modulpfad setzen
ENV PATH_TO_FX=/opt/javafx-sdk-17.0.14/lib

# Kopiere die JAR-Datei
COPY target/FileVault-shaded.jar /app/FileVault.jar

# Port für die API freigeben
EXPOSE 9090

# Start-Befehl für die API mit JavaFX-Konfiguration
CMD ["sh", "-c", "Xvfb :99 -screen 0 1024x768x24 -nolisten tcp & DISPLAY=:99 java -Djava.awt.headless=true --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml -jar /app/FileVault.jar --api-port=9090"] 