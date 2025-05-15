# FileVault

FileVault ist eine moderne Java-Anwendung zur sicheren Dateiverschlüsselung und -verwaltung. Mit einer benutzerfreundlichen Oberfläche können Sie vertrauliche Dateien in virtuellen Ordnern organisieren und mit starker Verschlüsselung schützen.

Das Projekt entstand im Rahmen des Kurses "Objektorientierte Programmierung II: Datenstrukturen und Java-Klassenbibliothek" und durchlief mehrere Entwicklungsphasen von der grundlegenden Verschlüsselung über Datenbankintegration bis hin zum JavaFX-Interface.

🔗 **Live Demo & Dokumentation**: [https://GulfGulfinson.github.io/fileVault](https://GulfGulfinson.github.io/fileVault)

## 🔐 Hauptfunktionen

- **Sichere Verschlüsselung** mit AES-256-GCM für maximalen Datenschutz
- **Intuitive Dateiverwaltung** in virtuellen Ordnern mit Drag & Drop
- **Cross-Platform Kompatibilität** für Windows, macOS und Linux
- **Responsive UI** mit modernem JavaFX-Design und Dark Mode
- **Passwort-basierte Authentifizierung** mit sicherer Schlüsselableitung
- **Integritätsschutz** durch GCM-Authentifizierung

## 🔧 Technische Details

### Sicherheitsfunktionen
- **Verschlüsselungsalgorithmus**: AES-256-GCM (Galois/Counter Mode)
- **Schlüsselableitung**: PBKDF2 mit HMAC-SHA256, 65.536 Iterationen
- **Zufallszahlengenerierung**: Kryptografisch sicher für IV (96 Bit) und Salts
- **Authentifizierung**: 128-Bit Auth-Tag zur Integritätsprüfung
- **Datenschutz**: Keine Speicherung von Klartextpasswörtern

### Datenspeicherung
- **Verschlüsselte Daten**: `~/.filevault/data/` (plattformunabhängig)
- **Metadaten**: SQLite-Datenbank in `~/.filevault/vault.db`
- **Backups**: Automatische Datensicherung (konfigurierbar)

## 💻 Installation

### Option 1: Release herunterladen (empfohlen)
1. Laden Sie die [neueste Version](https://github.com/GulfGulfinson/fileVault/releases) herunter
2. Entpacken Sie die ZIP-Datei
3. Starten Sie die Anwendung:
   ```
   ./start.sh    # Für Linux/Mac
   start.bat     # Für Windows
   ```

> **Wichtig**: Die Anwendung benötigt JavaFX, das im Release-Paket im Ordner `lib/javafx-sdk-17.0.14` enthalten ist. Falls dieser Ordner fehlt, laden Sie das passende [JavaFX SDK 17.0.14](https://gluonhq.com/products/javafx/) herunter und entpacken Sie es in den `lib`-Ordner.

### Option 2: Aus dem Quellcode bauen
1. Voraussetzungen:
   - Java 17+ (OpenJDK oder Oracle JDK)
   - Maven 3.8+

2. Repository klonen:
   ```bash
   git clone https://github.com/GulfGulfinson/fileVault.git
   cd fileVault
   ```

3. Bauen und Starten:
   ```bash
   mvn clean package
   ./start.sh   # Linux/Mac
   start.bat    # Windows
   ```
   
   Alternativ:
   ```bash
   mvn javafx:run
   ```

> **Hinweis**: Bei der ersten Ausführung wird automatisch ein neuer Benutzer angelegt. 
> Für einen neuen Benutzer muss die bestehende Datenbank (`~/.filevault/vault.db`) gelöscht werden.

### Option 3: Docker Container

#### Docker-Befehl
```bash
# Container starten und API auf Port 9090 verfügbar machen
docker pull ghcr.io/gulfgulfinson/filevault:latest
docker run -d -p 9090:9090 -v filevault-data:/root/.filevault --name filevault ghcr.io/gulfgulfinson/filevault:latest
```

#### Mit Docker Compose (empfohlen)
1. Erstellen Sie eine `docker-compose.yml` Datei oder verwenden Sie die mitgelieferte:
   ```yaml
   version: '3.8'
   
   services:
     filevault:
       image: ghcr.io/gulfgulfinson/filevault:latest
       container_name: filevault
       ports:
         - "9090:9090"
       volumes:
         - filevault-data:/root/.filevault
       restart: unless-stopped
   
   volumes:
     filevault-data:
       name: filevault-data
   ```

2. Starten Sie FileVault mit Docker Compose:
   ```bash
   docker-compose up -d
   ```

3. Zugriff auf die Anwendung:
   - Die FileVault-API ist unter http://localhost:9090 verfügbar
   - Verwenden Sie einen REST-Client oder die Desktop-Anwendung, um mit der API zu interagieren

#### GitHub Packages
```xml
<repositories>
    <repository>
        <id>github</id>
        <n>GitHub GulfGulfinson Apache Maven Packages</n>
        <url>https://maven.pkg.github.com/GulfGulfinson/fileVault</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.filevault</groupId>
    <artifactId>FileVault</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 Verwendung

1. **Anmeldung**: Starten Sie die App und erstellen Sie ein sicheres Master-Passwort
2. **Dateien importieren**: Ziehen Sie Dateien in die App oder nutzen Sie den Import-Dialog
3. **Ordnerstruktur**: Erstellen Sie virtuelle Ordner zur Organisation
4. **Verschlüsselung**: Alle importierten Dateien werden automatisch verschlüsselt
5. **Entschlüsselung**: Exportieren Sie Dateien, um sie im Originalformat zu nutzen

## 📂 Projektstruktur

```
src/main/java/com/filevault/
├── controller/     # UI-Controller und Anwendungslogik
├── model/          # Datenmodelle und Objektstrukturen
├── view/           # JavaFX FXML und UI-Komponenten
├── security/       # Verschlüsselung und Authentifizierung
├── storage/        # Datei- und Datenbankverwaltung
└── util/           # Hilfsfunktionen und Utilities
```

## 📖 Dokumentation

- **Website**: [https://GulfGulfinson.github.io/fileVault](https://GulfGulfinson.github.io/fileVault)
- **JavaDoc**: [Vollständige API-Dokumentation](https://GulfGulfinson.github.io/fileVault/javadoc/main.html)
- **Live Demo**: [WebAssembly-Demo im Browser](https://GulfGulfinson.github.io/fileVault#wasm-demo-container)

## 🤝 Beitragen

Beiträge zum Projekt sind willkommen! Weitere Informationen finden Sie in der [CONTRIBUTING.md](docs/markdown/CONTRIBUTING.md).

## 📄 Lizenz

Dieses Projekt steht unter der MIT-Lizenz. Details finden Sie in der [LICENSE](docs/markdown/LICENSE.md) Datei.

---

Entwickelt von Phillip Schneider | [GitHub Profil](https://github.com/GulfGulfinson)
