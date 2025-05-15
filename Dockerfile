# Basis-Image mit OpenJDK 17
FROM eclipse-temurin:17-jre-alpine

# Metadaten
LABEL org.opencontainers.image.source="https://github.com/GulfGulfinson/fileVault"
LABEL org.opencontainers.image.description="FileVault - Sichere Dateiverwaltung"
LABEL org.opencontainers.image.licenses=MIT

# Arbeitsverzeichnis setzen
WORKDIR /app

# JavaFX-Abhängigkeiten für Alpine Linux
RUN apk add --no-cache \
    libxtst \
    libxi \
    libxrandr \
    libxrender \
    libxext \
    libxfixes \
    libx11 \
    fontconfig \
    ttf-dejavu

# Kopiere die JAR-Datei
COPY target/FileVault-shaded.jar /app/FileVault.jar

# Port für die API freigeben
EXPOSE 9090

# Start-Befehl für die API
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "/app/FileVault.jar", "--api-port=9090"] 