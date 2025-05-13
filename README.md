# Projekt für Java-II: FileVault

Dieses Projekt ist eine Anwendung zur sicheren Verwaltung und Verschluesselung von Dateien. Die Anwendung basiert auf Java (mit etwas CSS) und bietet Oberfläche für dieVerwaltung verschlüsselter Dateien in virtuellen Ordnern. Dieses Projekt ist im Rahmen des Kurses "Objektorientierte Programmierung II: Datenstrukturen und Java-Klassenbibliothek" entstanden.
Dabei habe ich mehrere Iterationen von reiner Verschlüsselung von Dateien über zur Einbindung einer Datenbank bis hin zum Anlegen eines Frontends durchlaufen.
--Phillip Schneider

🔗 **Projektwebsite**: [https://GulfGulfinson.github.io/fileVault](https://GulfGulfinson.github.io/fileVault)

## 1. Hauptfunktionen

- Sichere Verschlüsselung von Dateien mit AES-256-GCM
- Organisierte Dateiverwaltung in virtuellen Ordnern
- Plattformübergreifende Kompatibilität
- Benutzerfreundliche grafische Oberfläche mit JavaFx
- Sichere Passwort-basierte Authentifizierung

## 2. Technische Details

### 2.1 Verschlüsselung
- Algorithmus: AES-256-GCM (Galois/Counter Mode)
- Schlüsselableitung: PBKDF2 mit HMAC-SHA256
- Schlüssellänge: 256 Bit
- Iterationen: 65.536
- IV-Länge: 96 Bit
- Authentifizierungs-Tag: 128 Bit

### 2.2 Datenspeicherung
- Verschlüsselte Dateien: `~/.filevault/data/`
- Datenbank: `~/.filevault/vault.db`
- Die Pfade werden automatisch an das jeweilige Betriebssystem angepasst

### 2.3 Sicherheitsmerkmale
- Sichere Zufallszahlengenerierung für IVs und Salts
- Integritätsschutz durch GCM-Modus
- Passwort-basierte Authentifizierung
- Sichere Schlüsselableitung
- Keine Speicherung von Klartextpasswörtern

## 3. Installation
1. Java 11 oder höher (am Besten 17 oder 21)
2. Maven installieren
3. Einmalig: mvn clean package
4. Projekt bauen: .bat/.sh -Datei ausführen oder bash: "mvn javafx:run"
! Bei der ersten Ausführung wird ein neuer Benutzer angelegt
(Für einen neuen User muss die Datenbank gelöscht werden)

### 3.1 Installation mit Docker
Sie können FileVault auch als Docker-Container ausführen:

```bash
docker pull ghcr.io/GulfGulfinson/fileVault:latest
docker run -v ~/.filevault:/root/.filevault ghcr.io/GulfGulfinson/fileVault:latest
```

### 3.2 Installation über GitHub Packages
Das FileVault-Projekt wird in GitHub Packages als Maven-Paket bereitgestellt:

1. Fügen Sie in Ihrer `pom.xml` das GitHub Package Repository hinzu:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub GulfGulfinson Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/GulfGulfinson/fileVault</url>
    </repository>
</repositories>
```

2. Fügen Sie die Abhängigkeit hinzu:

```xml
<dependency>
    <groupId>com.filevault</groupId>
    <artifactId>FileVault</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 4. Verwendung
1. Anmeldung mit Passwort
2. Erstellen von virtuellen Ordnern
3. Importieren von Dateien in die Ordner
4. Dateien können jederzeit exportiert und entschlüsselt werden

## 5. Projekt Struktur

- `controller`: Anwendungs-Controller
- `model`: Daten Modelle
- `GUI`: JavaFX-GUI-Dateien
- `security`: Verschlüsselung und Passwort
- `storage`: Datenspeicherung und Datenbank
- `util`: Utility Klassen

## 6. Dokumentation

Die vollständige Dokumentation finden Sie auf unserer [GitHub Pages-Website](https://GulfGulfinson.github.io/fileVault).

## 7. Beitragen

Wenn Sie zum Projekt beitragen möchten, folgen Sie bitte unseren Richtlinien in der [CONTRIBUTING.md](CONTRIBUTING.md) Datei.
