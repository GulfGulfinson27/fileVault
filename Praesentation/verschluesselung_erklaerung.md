# Detaillierte Erklärung der Verschlüsselung in FileVault

## 1. Grundlegende Konzepte und Fachbegriffe

### 1.1 Verschlüsselungsgrundlagen
- **Verschlüsselung (Encryption)**: 
  - Prozess der Umwandlung von lesbaren Daten (Klartext) in unlesbare Daten (Chiffretext)
  - Verwendet mathematische Algorithmen und Schlüssel
  - Ziel: Schutz der Vertraulichkeit von Daten

- **Entschlüsselung (Decryption)**:
  - Umkehrprozess der Verschlüsselung
  - Wandelt Chiffretext zurück in Klartext
  - Erfordert den korrekten Schlüssel

- **Symmetrische Verschlüsselung**:
  - Verwendet den gleichen Schlüssel für Ver- und Entschlüsselung
  - Schneller als asymmetrische Verschlüsselung
  - Beispiel: AES (Advanced Encryption Standard)

### 1.2 Schlüsselkonzepte
- **Schlüssel (Key)**:
  - Geheime Information zur Ver- und Entschlüsselung
  - In FileVault: 256 Bit (32 Byte) lang
  - Muss geheim gehalten werden

- **Initialisierungsvektor (IV)**:
  - Zufälliger Wert, der die Verschlüsselung initialisiert
  - Verhindert, dass gleiche Klartexte zu gleichen Chiffretexten führen
  - In FileVault: 96 Bit (12 Byte) lang
  - Wird für jede Verschlüsselung neu generiert

- **Salt**:
  - Zufällige Daten, die bei der Schlüsselableitung hinzugefügt werden
  - Verhindert Rainbow-Table-Angriffe
  - In FileVault: mindestens 16 Byte lang
  - Wird für jeden Benutzer individuell generiert

## 2. Detaillierter Verschlüsselungsprozess

### 2.1 Passwortverarbeitung und Schlüsselableitung
1. **Benutzereingabe**:
   - Benutzer gibt sein Passwort ein
   - Passwort wird NICHT direkt als Schlüssel verwendet
   - Wird sofort nach der Verarbeitung aus dem Speicher gelöscht

2. **PBKDF2 (Password-Based Key Derivation Function 2)**:
   - Funktion zur sicheren Ableitung von Schlüsseln aus Passwörtern
   - Verwendet HMAC-SHA256 als Hash-Funktion
   - Führt 65.536 Iterationen durch
   - Verlangsamt Brute-Force-Angriffe erheblich

3. **Schlüsselableitung im Detail**:
   ```
   Passwort + Salt → PBKDF2 → 256-Bit Schlüssel
   ```
   - Salt wird aus der Datenbank geladen
   - Passwort wird mit Salt kombiniert
   - PBKDF2 wird 65.536 Mal ausgeführt
   - Ergebnis ist ein 256-Bit langer Schlüssel

### 2.2 AES-256-GCM Verschlüsselung
1. **AES (Advanced Encryption Standard)**:
   - Symmetrischer Blockchiffre
   - Verwendet 256-Bit Schlüssel
   - Verarbeitet Daten in 128-Bit Blöcken
   - Als sicher gegen aktuelle Angriffe eingestuft

2. **GCM (Galois/Counter Mode)**:
   - Authentifizierter Verschlüsselungsmodus
   - Kombiniert Verschlüsselung mit Authentifizierung
   - Erzeugt 128-Bit Authentifizierungs-Tag
   - Schützt vor Manipulationen

3. **Verschlüsselungsprozess im Detail**:
   ```
   Klartext + IV + Schlüssel → AES-256-GCM → Chiffretext + Tag
   ```
   - IV wird zufällig generiert
   - Datei wird in Blöcke aufgeteilt
   - Jeder Block wird verschlüsselt
   - Authentifizierungs-Tag wird berechnet

### 2.3 Speicherung und Metadaten
1. **Dateispeicherung**:
   - Verschlüsselte Dateien: `~/.filevault/data/`
   - Jede Datei hat eindeutige Kennung
   - Originaldatei wird sicher gelöscht

2. **Metadaten in SQLite-Datenbank**:
   - Benutzerinformationen
   - IV für jede Datei
   - Salt für jeden Benutzer
   - Authentifizierungs-Tags
   - Dateistruktur und Berechtigungen

## 3. Entschlüsselungsprozess

### 3.1 Authentifizierung
1. **Passwortvalidierung**:
   - Benutzer gibt Passwort ein
   - System lädt Salt aus Datenbank
   - PBKDF2 wird ausgeführt
   - Ergebnis wird mit gespeichertem Hash verglichen

2. **Schlüsselwiederherstellung**:
   - Nach erfolgreicher Authentifizierung
   - Gleicher Prozess wie bei der Verschlüsselung
   - 256-Bit Schlüssel wird abgeleitet

### 3.2 Dateientschlüsselung
1. **Datenwiederherstellung**:
   - IV wird aus Datenbank geladen
   - Verschlüsselte Datei wird geladen
   - Authentifizierungs-Tag wird überprüft

2. **Entschlüsselungsprozess**:
   ```
   Chiffretext + IV + Schlüssel → AES-256-GCM → Klartext
   ```
   - GCM verifiziert Authentifizierungs-Tag
   - Bei erfolgreicher Verifizierung wird entschlüsselt
   - Entschlüsselte Daten werden temporär im Speicher gehalten

## 4. Sicherheitsmaßnahmen und Schutzmechanismen

### 4.1 Schutz gegen Angriffe
1. **Brute-Force-Angriffe**:
   - PBKDF2 mit 65.536 Iterationen
   - Verlangsamt Angriffe erheblich
   - Erfordert enorme Rechenleistung

2. **Rainbow-Table-Angriffe**:
   - Verwendung von Salts
   - Jeder Benutzer hat eigenen Salt
   - Macht vorgefertigte Tabellen wirkungslos

3. **Replay-Angriffe**:
   - Zufällige IVs für jede Verschlüsselung
   - Verhindert Wiederverwendung von Chiffretexten
   - Jede Verschlüsselung ist einzigartig

### 4.2 Datenschutz
1. **Speicherung sensibler Daten**:
   - Keine Klartextpasswörter
   - Schlüssel nur im Arbeitsspeicher
   - Sichere Löschung nach Verwendung

2. **Temporäre Dateien**:
   - Werden sicher gelöscht
   - Mehrfaches Überschreiben
   - Verhinderung von Datenwiederherstellung

## 5. Technische Spezifikationen

### 5.1 Algorithmus-Parameter
1. **AES-256-GCM**:
   - Schlüssellänge: 256 Bit
   - Blockgröße: 128 Bit
   - IV-Länge: 96 Bit
   - Tag-Länge: 128 Bit

2. **PBKDF2**:
   - Hash-Funktion: HMAC-SHA256
   - Iterationen: 65.536
   - Salt-Länge: ≥ 16 Byte
   - Ausgabelänge: 32 Byte

### 5.2 Dateisystem-Struktur
```
~/.filevault/
├── data/                    # Verschlüsselte Dateien
│   ├── user1/              # Benutzer-spezifischer Ordner
│   │   ├── folder1/        # Virtuelle Ordner
│   │   └── folder2/
│   └── user2/
└── vault.db                # SQLite-Datenbank
    ├── users              # Benutzerinformationen
    │   ├── id
    │   ├── username
    │   └── salt
    ├── folders            # Ordnerstruktur
    │   ├── id
    │   ├── name
    │   └── owner_id
    └── files              # Dateimetadaten
        ├── id
        ├── name
        ├── folder_id
        ├── iv
        └── auth_tag
```

## 6. Glossar der Fachbegriffe

### 6.1 Verschlüsselungsbegriffe
- **AES (Advanced Encryption Standard)**: 
  - Symmetrischer Verschlüsselungsalgorithmus
  - Verwendet 256-Bit Schlüssel
  - Als sicher gegen aktuelle Angriffe eingestuft

- **GCM (Galois/Counter Mode)**:
  - Authentifizierter Verschlüsselungsmodus
  - Kombiniert Verschlüsselung mit Integritätsschutz
  - Erzeugt Authentifizierungs-Tag

- **PBKDF2 (Password-Based Key Derivation Function 2)**:
  - Funktion zur sicheren Ableitung von Schlüsseln aus Passwörtern
  - Verwendet viele Iterationen zur Verlangsamung von Angriffen
  - Kombiniert Passwort mit Salt

### 6.2 Sicherheitsbegriffe
- **Authentifizierungs-Tag**:
  - Prüfsumme zur Verifizierung der Datenintegrität
  - Wird bei GCM automatisch erzeugt
  - 128 Bit lang

- **Brute-Force-Angriff**:
  - Versuch, ein Passwort durch Ausprobieren zu erraten
  - Wird durch PBKDF2 erschwert
  - Benötigt enorme Rechenleistung

- **Rainbow-Table**:
  - Vorgefertigte Tabelle mit Passwort-Hashes
  - Wird durch Salts wirkungslos gemacht
  - Beschleunigt normalerweise Passwort-Cracking

### 6.3 Technische Begriffe
- **HMAC-SHA256**:
  - Kryptographische Hash-Funktion
  - Erzeugt 256-Bit lange Hash-Werte
  - Wird in PBKDF2 verwendet

- **SQLite**:
  - Leichte, serverlose Datenbank
  - Speichert Metadaten und Konfiguration
  - Einfache Integration in Java

- **JavaFX**:
  - Moderne GUI-Bibliothek für Java
  - Ermöglicht benutzerfreundliche Oberfläche
  - Plattformunabhängig 