# FileVault API Dokumentation

## Übersicht
Die FileVault API bietet eine Reihe von Endpunkten, um Dateien und Ordner zu verwalten, sowie Authentifizierungsmechanismen, um den Zugriff zu sichern. Diese Dokumentation beschreibt die verfügbaren Endpunkte, ihre Funktionalität und wie sie verwendet werden können.

---

## Authentifizierung

### Endpunkt: `/api/auth`
- **Methode**: `POST`
- **Beschreibung**: Authentifiziert den Benutzer und gibt ein Token zurück, das für alle weiteren Anfragen erforderlich ist.
- **Anfrage-Body**:
  ```json
  {
    "password": "<MASTER_PASSWORD>"
  }
  ```
- **Antworten**:
  - **200 OK**: Gibt ein Token zurück.
    ```json
    {
      "token": "<TOKEN>"
    }
    ```
  - **401 Unauthorized**: Ungültiges Passwort.
    ```json
    {
      "error": "Ungültiges Passwort. Zugriff verweigert."
    }
    ```

---

## Endpunkte

### 1. `/api/info`
- **Methode**: `GET`
- **Beschreibung**: Gibt allgemeine Informationen zur API zurück.
- **Header**:
  - `Authorization: <TOKEN>`
- **Antworten**:
  - **200 OK**: Erfolgreiche Anfrage.
    ```text
    Willkommen bei der FileVault API!
    ```
  - **401 Unauthorized**: Fehlendes oder ungültiges Token.

### 2. `/api/folders`
- **Methode**: `GET`, `POST`, `PUT`, `DELETE`
- **Beschreibung**: Verwalten von virtuellen Ordnern.
- **Header**:
  - `Authorization: <TOKEN>`
- **Antworten**:
  - **200 OK**: Erfolgreiche Anfrage.
  - **401 Unauthorized**: Fehlendes oder ungültiges Token.

### 3. `/api/files`
- **Methode**: `GET`, `POST`, `PUT`, `DELETE`
- **Beschreibung**: Verwalten von Dateien.
- **Header**:
  - `Authorization: <TOKEN>`
- **Antworten**:
  - **200 OK**: Erfolgreiche Anfrage.
  - **401 Unauthorized**: Fehlendes oder ungültiges Token.

---

## Beispielanfragen

### Authentifizierung
**Anfrage:**
```bash
curl -X POST -d "password=11111111" http://localhost:9090/api/auth
```
**Antwort:**
```json
{
  "token": "f3875e91-e8df-4b4d-9c0a-3b990f9202a5"
}
```

### Zugriff auf `/api/info`
**Anfrage:**
```bash
curl -X GET -H "Authorization: f3875e91-e8df-4b4d-9c0a-3b990f9202a5" http://localhost:9090/api/info
```
**Antwort:**
```text
Willkommen bei der FileVault API!
```

### Zugriff auf `/api/folders`
**Anfrage:**
```bash
curl -X GET -H "Authorization: f3875e91-e8df-4b4d-9c0a-3b990f9202a5" http://localhost:9090/api/folders
```
**Antwort:**
```json
[
  {"id":1,"name":"Documents"},
  {"id":2,"name":"Pictures"}
]
```

### Zugriff auf `/api/files`
**Anfrage:**
```bash
curl -X GET -H "Authorization: f3875e91-e8df-4b4d-9c0a-3b990f9202a5" http://localhost:9090/api/files
```
**Antwort:**
```json
[
  {"id":1,"name":"file1.txt"},
  {"id":2,"name":"file2.jpg"}
]
```

---

## Hinweise
- Alle Anfragen an geschützte Endpunkte erfordern ein gültiges Token im `Authorization`-Header.
- Das Masterpasswort für die Authentifizierung lautet `11111111` (kann in der Implementierung geändert werden).

---

Diese Dokumentation wird regelmäßig aktualisiert, um neue Funktionen und Änderungen zu berücksichtigen.