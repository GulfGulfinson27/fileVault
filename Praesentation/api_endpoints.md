# FileVault API Endpoints

## Übersicht
Die FileVault API bietet verschiedene Endpunkte, um mit den Daten der FileVault-Datenbank zu interagieren. Nachfolgend sind die verfügbaren Endpunkte und deren Beschreibung aufgeführt.

---

## Endpunkte

### 1. `/api/info`
- **Methode:** GET
- **Beschreibung:** Gibt eine Willkommensnachricht zurück, um zu bestätigen, dass der Server läuft.
- **Beispielantwort:**
  ```json
  "Willkommen bei der FileVault API!"
  ```

---

### 2. `/api/folders`
- **Methode:** GET
- **Beschreibung:** Gibt eine Liste aller Ordner in der Datenbank zurück.
- **Beispielantwort:**
  ```json
  [
    {"id": 1, "name": "Dokumente"},
    {"id": 2, "name": "Bilder"}
  ]
  ```
- **Fehler:**
  - **500 Internal Server Error:** Tritt auf, wenn ein Fehler bei der Datenbankabfrage auftritt.

---

## Hinweise
- Alle Endpunkte verwenden JSON als Antwortformat.
- Stellen Sie sicher, dass die Datenbank korrekt initialisiert ist, bevor Sie die API verwenden.