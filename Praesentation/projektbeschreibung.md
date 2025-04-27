# FileVault - Sichere Dateiverwaltung

## 1. Projektübersicht
FileVault ist eine Java-basierte Anwendung zur sicheren Verwaltung und Verschlüsselung von Dateien. Das Projekt wurde im Rahmen des Kurses "Objektorientierte Programmierung II" entwickelt und bietet eine benutzerfreundliche Lösung für die sichere Speicherung sensibler Daten.

## 2. Kernfunktionen
### 2.1 Sicherheitsfunktionen
- **Verschlüsselung**: 
  - Verwendet AES-256-GCM (Advanced Encryption Standard)
  - 256-Bit-Schlüssellänge
  - Integritätsschutz durch GCM-Modus
  - Sichere Schlüsselableitung mit PBKDF2 und HMAC-SHA256
  - 65.536 Iterationen für erhöhte Sicherheit

### 2.2 Benutzerfunktionen
- **Dateiverwaltung**:
  - Erstellung virtueller Ordner
  - Import und Export von Dateien
  - Verschlüsselte Speicherung
  - Plattformübergreifende Kompatibilität

### 2.3 Benutzeroberfläche
- **JavaFX-basierte GUI**:
  - Moderne und intuitive Benutzeroberfläche
  - Einfache Navigation
  - Sichere Passwort-basierte Authentifizierung

### 2.4 Logging
- **Dateibasierte Protokollierung**:
  - Loggt wichtige Aktionen und Fehler in einer rotierenden Logdatei
  - Speicherort: `logs/filevault_log.log`
  - Unterstützt verschiedene Log-Level (INFO, WARN, SEVERE)
  - Die ältesten Logs werden überschrieben, wenn die Datei ihre maximale Größe erreicht
  - Ermöglicht einfache Fehlerdiagnose und Überwachung

## 3. Technische Architektur
### 3.1 Projektstruktur
- **Controller**: Steuert die Anwendungslogik
- **Model**: Datenmodelle und Geschäftslogik
- **View**: JavaFX-Komponenten für die Benutzeroberfläche
- **Security**: Verschlüsselungs- und Authentifizierungsfunktionen
- **Storage**: Datenbank- und Dateispeicherung
- **Util**: Hilfsfunktionen und Utilities

### 3.2 Datenspeicherung
- **Verschlüsselte Dateien**: `~/.filevault/data/`
- **Datenbank**: `~/.filevault/vault.db`
- **Automatische Pfadanpassung** für verschiedene Betriebssysteme

## 4. Sicherheitskonzept
### 4.1 Verschlüsselung
- **AES-256-GCM** als Hauptverschlüsselungsalgorithmus
- **96-Bit Initialisierungsvektor (IV)**
- **128-Bit Authentifizierungs-Tag**
- **Sichere Zufallszahlengenerierung** für IVs und Salts

### 4.2 Authentifizierung
- **Passwort-basierte Authentifizierung**
- **Keine Speicherung von Klartextpasswörtern**
- **Sichere Schlüsselableitung** mit PBKDF2

## 5. Technische Anforderungen
- Java 11 oder höher (empfohlen: Java 17 oder 21)
- Maven als Build-Tool
- Plattformübergreifende Kompatibilität

## 6. Installation und Verwendung
### 6.1 Installation
1. Java-Entwicklungsumgebung einrichten
2. Maven installieren
3. Projekt bauen mit `mvn clean package`
4. Anwendung starten mit `start.sh` oder `start.bat`

### 6.2 Erste Schritte
- Erster Start erstellt automatisch einen neuen Benutzer
- Anmeldung mit Passwort
- Erstellung virtueller Ordner
- Import und Export von Dateien

## 7. Besondere Merkmale
- **Hohe Sicherheitsstandards** durch moderne Verschlüsselungsverfahren
- **Benutzerfreundlichkeit** durch intuitive GUI
- **Flexibilität** durch virtuelle Ordnerstruktur
- **Plattformunabhängigkeit** durch Java-Implementierung

## 8. Entwicklungshistorie
Das Projekt durchlief mehrere Entwicklungsphasen:
1. Grundlegende Dateiverschlüsselung
2. Integration einer Datenbank
3. Entwicklung einer grafischen Benutzeroberfläche
4. Optimierung der Sicherheitsfunktionen

# Detaillierte Projektstruktur von FileVault

## 1. Hauptpakete und ihre Verantwortlichkeiten

### 1.1 Core-Paket (`com.filevault.core`)
- **FileVaultApp.java**: Hauptklasse der Anwendung
  - Initialisiert die JavaFX-Anwendung
  - Startet die Benutzeroberfläche
  - Verwaltet den Anwendungslebenszyklus

### 1.2 GUI-Paket (`com.filevault.gui`)
- **LoginController.java**: 
  - Handhabt die Benutzeranmeldung
  - Validierung der Anmeldedaten
  - Weiterleitung zum Hauptmenü

- **MainController.java**:
  - Steuert die Hauptansicht der Anwendung
  - Verwaltet die Ordnerstruktur
  - Koordiniert Dateioperationen

- **FolderController.java**:
  - Verwaltet einzelne Ordner
  - Handhabt Dateiimport und -export
  - Zeigt Ordnerinhalte an

### 1.3 Model-Paket (`com.filevault.model`)
- **User.java**:
  - Repräsentiert Benutzerdaten
  - Speichert Benutzerinformationen
  - Verwaltet Benutzerberechtigungen

- **Folder.java**:
  - Repräsentiert virtuelle Ordner
  - Speichert Ordnerstruktur
  - Verwaltet Dateizugriffe

- **File.java**:
  - Repräsentiert verschlüsselte Dateien
  - Speichert Metadaten
  - Handhabt Verschlüsselungsstatus

### 1.4 Security-Paket (`com.filevault.security`)
- **EncryptionService.java**:
  - Implementiert AES-256-GCM Verschlüsselung
  - Handhabt Schlüsselgenerierung
  - Verwaltet Verschlüsselungsoperationen

- **PasswordService.java**:
  - Verarbeitet Passwort-Hashing
  - Implementiert PBKDF2
  - Verwaltet Authentifizierung

### 1.5 Storage-Paket (`com.filevault.storage`)
- **DatabaseService.java**:
  - Verwaltet SQLite-Datenbank
  - Speichert Benutzer- und Ordnerdaten
  - Handhabt Datenbankoperationen

- **FileStorageService.java**:
  - Verwaltet Dateispeicherung
  - Handhabt Dateioperationen
  - Koordiniert Verschlüsselung/Entschlüsselung

### 1.6 Util-Paket (`com.filevault.util`)
- **FileUtils.java**:
  - Hilfsfunktionen für Dateioperationen
  - Pfadverarbeitung
  - Dateityp-Validierung

- **SecurityUtils.java**:
  - Sicherheitsbezogene Hilfsfunktionen
  - Zufallszahlengenerierung
  - Schlüsselverarbeitung

## 2. Teststruktur

### 2.1 Modell-Tests (`com.filevault.model`)
- **UserTest.java**:
  - Testet Benutzererstellung
  - Validierung von Benutzerdaten
  - Berechtigungsprüfungen

- **FolderTest.java**:
  - Testet Ordnererstellung
  - Dateiverwaltung in Ordnern
  - Ordnerstruktur-Validierung

### 2.2 Utility-Tests (`com.filevault.util`)
- **FileUtilsTest.java**:
  - Testet Dateioperationen
  - Pfadverarbeitung
  - Dateityp-Validierung

## 3. Frontend-Komponenten

### 3.1 JavaFX-Benutzeroberfläche
- **Login-Screen**:
  - Benutzeranmeldung
  - Passworteingabe
  - Fehlerbehandlung

- **Hauptmenü**:
  - Ordnerübersicht
  - Dateiverwaltung
  - Benutzereinstellungen

- **Ordneransicht**:
  - Dateiliste
  - Import/Export-Funktionen
  - Kontextmenüs

### 3.2 Styling und Layout
- **CSS-Dateien**:
  - Modernes Design
  - Responsive Layout
  - Benutzerfreundliche Oberfläche

## 4. Build- und Konfigurationsdateien

### 4.1 Maven-Konfiguration
- **pom.xml**:
  - Projektabhängigkeiten
  - Build-Konfiguration
  - Plugin-Einstellungen

### 4.2 Startskripte
- **start.sh** (Linux/Mac):
  - Startet die Anwendung unter Unix-Systemen
  - Setzt Umgebungsvariablen
  - Handhabt Java-Aufruf

- **start.bat** (Windows):
  - Startet die Anwendung unter Windows
  - Windows-spezifische Konfiguration
  - Batch-Processing

## 5. Datenstruktur

### 5.1 Datenbank-Schema
- **Benutzer-Tabelle**:
  - Benutzer-ID
  - Verschlüsselte Passwörter
  - Benutzereinstellungen

- **Ordner-Tabelle**:
  - Ordner-ID
  - Benutzerzuordnung
  - Metadaten

- **Datei-Tabelle**:
  - Datei-ID
  - Ordnerzuordnung
  - Verschlüsselungsinformationen

### 5.2 Dateisystem-Struktur
- **Verschlüsselte Dateien**:
  - Speicherort: `~/.filevault/data/`
  - Verschlüsselte Inhalte
  - Metadaten