# FileVault API Beispiele für Linux (curl)

Dieses Dokument enthält Beispiele für die Verwendung der FileVault API mit `curl` unter Linux.

## Basiseinstellung

Die folgenden Beispiele gehen davon aus, dass der FileVault API-Server unter `localhost:9090` läuft. Falls Sie einen anderen Host oder Port verwenden, passen Sie die Befehle entsprechend an.

## Authentifizierung

### Anmeldung

```sh
curl -X POST http://localhost:9090/api/auth \
  -H "Content-Type: application/json" \
  -d '{"password":"11111111"}'
```

**Erwartete Antwort**:

```json
{"token":"d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"}
```

## Ordnerverwaltung

### Alle Ordner auflisten

```sh
curl -X GET http://localhost:9090/api/folders \
  -H "Authorization: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
```

**Erwartete Antwort**:

```json
[{"id":1,"name":"Persönlich","parentFolderId":0},{"id":2,"name":"Arbeit","parentFolderId":0},{"id":3,"name":"Dokumente","parentFolderId":1}]
```

### Neuen Ordner erstellen

#### Ordner in der Root-Ebene erstellen

```sh
curl -X POST http://localhost:9090/api/folders \
  -H "Authorization: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8" \
  -H "Content-Type: application/json" \
  -d '{"name":"Neuer Ordner"}'
```

#### Ordner innerhalb eines vorhandenen Ordners erstellen

```sh
curl -X POST http://localhost:9090/api/folders \
  -H "Authorization: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8" \
  -H "Content-Type: application/json" \
  -d '{"name":"Unterordner","parentFolderId":1}'
```

**Erwartete Antwort**:

```json
{"id":4,"name":"Unterordner","parentFolderId":1}
```

### Ordnernamen aktualisieren

```sh
curl -X PUT http://localhost:9090/api/folders \
  -H "Authorization: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8" \
  -H "Content-Type: application/json" \
  -d '{"id":4,"name":"Umbenannter Ordner"}'
```

**Erwartete Antwort**:

```json
{"id":4,"name":"Umbenannter Ordner"}
```

### Ordner löschen

```sh
curl -X DELETE "http://localhost:9090/api/folders?id=4" \
  -H "Authorization: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
```

**Erwartete Antwort**:

```
Ordner erfolgreich gelöscht.
```

**Mögliche Fehlermeldung (bei Ordner mit Inhalt)**:

```
Ordner mit Inhalt können nicht über die API gelöscht werden. Bitte verwenden Sie die grafische Benutzeroberfläche (GUI), um Ordner mit Unterordnern oder Dateien zu löschen.
```

## Dateiverwaltung

### Alle Dateien auflisten

```sh
curl -X GET http://localhost:9090/api/files \
  -H "Authorization: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
```

**Erwartete Antwort**:

```json
[{"id":1,"name":"bericht.pdf","folderId":2},{"id":2,"name":"notizen.txt","folderId":1}]
```

## Fehlerbehandlung

### Beispiel für ungültige Authentifizierung

```sh
curl -X POST http://localhost:9090/api/auth \
  -H "Content-Type: application/json" \
  -d '{"password":"falschespasswort"}'
```

**Erwartete Antwort**:

```json
{"error":"Ungültiges Passwort. Zugriff verweigert."}
```

### Beispiel für eine Anfrage ohne Token

```sh
curl -X GET http://localhost:9090/api/folders
```

**Erwartete Antwort**:

```json
{"error":"Unauthorized: Invalid or missing authentication token."}
```

### Beispiel für eine Anfrage mit ungültigem Token

```sh
curl -X GET http://localhost:9090/api/folders \
  -H "Authorization: ungültigestoken"
```

**Erwartete Antwort**:

```json
{"error":"Unauthorized: Invalid or missing authentication token."}
```

## Testen des Web-Interfaces

Sie können auch auf das Web-Interface zugreifen, um die API interaktiv zu testen:

```sh
curl http://localhost:9090/
```

Dies gibt die HTML-Seite des Web-Interfaces zurück, die Sie in einem Browser öffnen können. 