# Technische Architektur-Dokumentation für FileVault

## 1. Komponenten und ihre Beziehungen

### 1.1 Core-Komponenten
- **FileVaultApp** (Hauptklasse)
  - Verantwortlichkeiten:
    - Initialisierung der JavaFX-Anwendung
    - Start der Benutzeroberfläche
    - Lebenszyklus-Management
  - Abhängigkeiten:
    - JavaFX
    - Maven
  - Verbindungen:
    - → LoginController (initialisiert)
    - → MainController (initialisiert)

### 1.2 GUI-Komponenten
- **LoginController**
  - Verantwortlichkeiten:
    - Benutzeranmeldung
    - Passwortvalidierung
    - Weiterleitung zum Hauptmenü
  - Abhängigkeiten:
    - UserManager
    - PasswordService
  - Verbindungen:
    - → MainController (nach erfolgreicher Anmeldung)
    - ← UserManager (Authentifizierung)
    - ← PasswordService (Passwortverarbeitung)

- **MainController**
  - Verantwortlichkeiten:
    - Hauptansicht der Anwendung
    - Ordnerstruktur-Management
    - Dateioperationen-Koordination
  - Abhängigkeiten:
    - FolderController
    - DatabaseService
  - Verbindungen:
    - → FolderController (Ordnerverwaltung)
    - ← DatabaseService (Datenpersistenz)

- **FolderController**
  - Verantwortlichkeiten:
    - Einzelne Ordnerverwaltung
    - Dateiimport/-export
    - Ordnerinhaltsanzeige
  - Abhängigkeiten:
    - FileStorageService
    - EncryptionService
  - Verbindungen:
    - → FileStorageService (Dateioperationen)
    - → EncryptionService (Verschlüsselung)

### 1.3 Model-Komponenten
- **User**
  - Verantwortlichkeiten:
    - Benutzerdaten-Repräsentation
    - Berechtigungsverwaltung
  - Abhängigkeiten:
    - DatabaseService
  - Verbindungen:
    - ← DatabaseService (Persistenz)
    - → PasswordService (Authentifizierung)

- **Folder**
  - Verantwortlichkeiten:
    - Virtuelle Ordner-Repräsentation
    - Dateizugriffsverwaltung
  - Abhängigkeiten:
    - DatabaseService
    - FileStorageService
  - Verbindungen:
    - ← DatabaseService (Persistenz)
    - → FileStorageService (Dateioperationen)

- **File**
  - Verantwortlichkeiten:
    - Verschlüsselte Datei-Repräsentation
    - Metadatenverwaltung
  - Abhängigkeiten:
    - EncryptionService
    - FileStorageService
  - Verbindungen:
    - → EncryptionService (Verschlüsselung)
    - → FileStorageService (Speicherung)

### 1.4 Security-Komponenten
- **EncryptionService**
  - Verantwortlichkeiten:
    - AES-256-GCM Verschlüsselung
    - Schlüsselgenerierung
    - Verschlüsselungsoperationen
  - Abhängigkeiten:
    - SecurityUtils
  - Verbindungen:
    - → SecurityUtils (Sicherheitsfunktionen)
    - ← File (Verschlüsselungsanfragen)

- **PasswordService**
  - Verantwortlichkeiten:
    - Passwort-Hashing
    - PBKDF2-Implementierung
    - Authentifizierung
  - Abhängigkeiten:
    - SecurityUtils
  - Verbindungen:
    - → SecurityUtils (Sicherheitsfunktionen)
    - ← User (Authentifizierung)

### 1.5 Storage-Komponenten
- **DatabaseService**
  - Verantwortlichkeiten:
    - SQLite-Datenbankverwaltung
    - Datenpersistenz
    - Datenbankoperationen
  - Abhängigkeiten:
    - SQLite JDBC
  - Verbindungen:
    - → User (Benutzerdaten)
    - → Folder (Ordnerdaten)
    - → File (Dateimetadaten)

- **FileStorageService**
  - Verantwortlichkeiten:
    - Dateispeicherung
    - Dateioperationen
    - Verschlüsselungskoordination
  - Abhängigkeiten:
    - FileUtils
    - EncryptionService
  - Verbindungen:
    - → FileUtils (Dateioperationen)
    - → EncryptionService (Verschlüsselung)

### 1.6 Utility-Komponenten
- **FileUtils**
  - Verantwortlichkeiten:
    - Dateioperationen
    - Pfadverarbeitung
    - Dateityp-Validierung
  - Verbindungen:
    - ← FileStorageService (Dateioperationen)

- **SecurityUtils**
  - Verantwortlichkeiten:
    - Sicherheitsfunktionen
    - Zufallszahlengenerierung
    - Schlüsselverarbeitung
  - Verbindungen:
    - ← EncryptionService (Verschlüsselung)
    - ← PasswordService (Authentifizierung)

## 2. Datenflüsse

### 2.1 Authentifizierungsfluss
1. Benutzer → LoginController (Anmeldedaten)
2. LoginController → PasswordService (Passwortvalidierung)
3. PasswordService → SecurityUtils (Passwortverarbeitung)
4. PasswordService → User (Authentifizierung)
5. User → DatabaseService (Benutzerdaten)

### 2.2 Dateioperationen-Fluss
1. Benutzer → FolderController (Dateioperation)
2. FolderController → FileStorageService (Dateiverarbeitung)
3. FileStorageService → EncryptionService (Verschlüsselung)
4. FileStorageService → FileUtils (Dateioperationen)
5. FileStorageService → DatabaseService (Metadaten)

### 2.3 Ordnerverwaltungsfluss
1. Benutzer → MainController (Ordneroperation)
2. MainController → FolderController (Ordnerverwaltung)
3. FolderController → DatabaseService (Ordnerdaten)
4. FolderController → FileStorageService (Dateioperationen)

## 3. Externe Abhängigkeiten

### 3.1 Bibliotheken
- JavaFX (GUI)
- SQLite JDBC (Datenbank)
- JUnit (Tests)
- Maven (Build)

### 3.2 Systemanforderungen
- Java 11+
- Maven
- Betriebssystem: Windows/Linux/Mac

## 4. Speicherstruktur

### 4.1 Dateisystem
```
~/.filevault/
├── data/           # Verschlüsselte Dateien
├── vault.db        # SQLite-Datenbank
└── config/         # Konfigurationsdateien
```

### 4.2 Datenbank-Schema
```sql
-- Benutzer-Tabelle
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username TEXT UNIQUE,
    password_hash TEXT,
    settings TEXT
);

-- Ordner-Tabelle
CREATE TABLE folders (
    id INTEGER PRIMARY KEY,
    user_id INTEGER,
    name TEXT,
    parent_id INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(id)
);

-- Datei-Tabelle
CREATE TABLE files (
    id INTEGER PRIMARY KEY,
    folder_id INTEGER,
    name TEXT,
    path TEXT,
    encryption_data TEXT,
    FOREIGN KEY(folder_id) REFERENCES folders(id)
);
```

## 5. Sicherheitsarchitektur

### 5.1 Verschlüsselungspipeline
1. Datei → FileStorageService
2. FileStorageService → EncryptionService
3. EncryptionService → SecurityUtils (Schlüsselgenerierung)
4. EncryptionService → File (Verschlüsselung)
5. File → FileStorageService (Speicherung)

### 5.2 Authentifizierungspipeline
1. LoginController → PasswordService
2. PasswordService → SecurityUtils (Passwortverarbeitung)
3. PasswordService → User (Validierung)
4. User → DatabaseService (Datenabgleich)

## 6. Testarchitektur

### 6.1 Testkomponenten
- **UserTest**
  - Testet Benutzererstellung
  - Testet Authentifizierung
  - Testet Berechtigungen

- **FolderTest**
  - Testet Ordnererstellung
  - Testet Dateiverwaltung
  - Testet Ordnerstruktur

- **FileUtilsTest**
  - Testet Dateioperationen
  - Testet Pfadverarbeitung
  - Testet Validierung

### 6.2 Testabhängigkeiten
- JUnit
- Mockito (für Abhängigkeiten)
- Testcontainers (für Datenbanktests) 