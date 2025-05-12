# FileVault API Dokumentation

## Übersicht

Die FileVault API ist eine RESTful API, die den Zugriff auf das FileVault-System ermöglicht. Sie bietet Funktionen zur Verwaltung von Ordnern und Dateien, die im FileVault-System gespeichert sind. Die API ist auf der Basis von Java implementiert und nutzt den integrierten HttpServer.

## Authentifizierung

Bevor Sie auf die meisten Endpunkte zugreifen können, müssen Sie sich bei der API authentifizieren.

### Authentifizierungsprozess

1. Senden Sie eine POST-Anfrage an den `/api/auth`-Endpunkt mit Ihrem Passwort.
2. Bei erfolgreicher Authentifizierung erhalten Sie ein Token zurück.
3. Dieses Token müssen Sie in allen nachfolgenden Anfragen im `Authorization`-Header mitschicken.

Das Master-Passwort für die Authentifizierung lautet: `11111111` (Hinweis: In einer Produktionsumgebung sollte ein sicheres Passwort verwendet werden)

### Token-Verwaltung

- Tokens werden im Speicher verwaltet und bleiben gültig, bis die Anwendung neu gestartet wird oder Sie sich ausloggen.
- Sie können Ihr Token im lokalen Speicher Ihres Browsers speichern, um es zwischen Sitzungen beizubehalten.

## Zugriffsbeschränkungen

- Der Endpunkt `/api/auth` kann ohne Authentifizierung aufgerufen werden.
- Alle anderen API-Endpunkte erfordern ein gültiges Authentifizierungstoken im `Authorization`-Header.
- Anfragen ohne gültiges Token werden mit einem 401-Fehler (Unauthorized) abgewiesen.

## API-Endpunkte

### Authentifizierung

#### POST /api/auth

Authentifiziert einen Benutzer und gibt ein Token zurück.

- **Anforderungsformat**: JSON mit Passwort
- **Antwortformat**: JSON mit Token
- **Auth erforderlich**: Nein

### Ordnerverwaltung

#### GET /api/folders

Listet alle verfügbaren Ordner auf.

- **Antwortformat**: JSON-Array mit Ordnern
- **Auth erforderlich**: Ja

#### POST /api/folders

Erstellt einen neuen Ordner.

- **Anforderungsformat**: JSON mit Ordnername und optionalem übergeordneten Ordner
- **Antwortformat**: JSON mit Informationen zum erstellten Ordner
- **Auth erforderlich**: Ja

#### PUT /api/folders

Aktualisiert einen bestehenden Ordner.

- **Anforderungsformat**: JSON mit Ordner-ID und neuem Namen
- **Antwortformat**: JSON mit aktualisierten Ordnerinformationen
- **Auth erforderlich**: Ja

#### DELETE /api/folders?id={id}

Löscht einen Ordner anhand seiner ID.

- **Antwortformat**: JSON mit Erfolgs- oder Fehlermeldung
- **Auth erforderlich**: Ja
- **Einschränkung**: Über die API können nur leere Ordner gelöscht werden. Ordner mit Unterordnern oder Dateien müssen über die grafische Benutzeroberfläche (GUI) gelöscht werden. Versuche, einen Ordner mit Inhalt über die API zu löschen, führen zu einer entsprechenden Fehlermeldung.

### Dateiverwaltung

#### GET /api/files

Listet alle verfügbaren Dateien auf.

- **Antwortformat**: JSON-Array mit Dateien
- **Auth erforderlich**: Ja

### Web-Interface

#### GET /

Bietet ein einfaches Web-Interface zum Testen der API.

- **Antwortformat**: HTML
- **Auth erforderlich**: Nein (aber Funktionalität innerhalb des Interfaces erfordert Authentifizierung)

## Wie die API im Web funktioniert

1. **Server-Start**: Die API wird auf einem angegebenen Port gestartet (standardmäßig 9090).
2. **HTTP-Anfragen**: Clients können HTTP-Anfragen an die verschiedenen Endpunkte senden.
3. **Authentifizierung**: Die meisten Anfragen werden über den `AuthMiddleware` geleitet, der das Token überprüft.
4. **Anfrageverarbeitung**: Je nach Endpunkt werden verschiedene Handler aufgerufen, die die Anfrage verarbeiten.
5. **Datenbankinteraktion**: Die API interagiert mit der Datenbank, um Ordner- und Dateiinformationen zu verwalten.
6. **Antwortgenerierung**: Die API sendet JSON-Antworten mit den angeforderten Daten oder Fehlermeldungen zurück.

## Fehlerbehandlung

Die API gibt verschiedene HTTP-Statuscodes zurück, um den Erfolg oder Misserfolg einer Anfrage anzuzeigen:

- 200: Erfolgreiche Anfrage
- 201: Ressource erfolgreich erstellt
- 401: Nicht autorisiert (fehlende oder ungültige Authentifizierung)
- 405: Methode nicht erlaubt (falsche HTTP-Methode für den Endpunkt)

Fehlermeldungen werden im JSON-Format zurückgegeben, um weitere Informationen über den Fehler zu liefern.

## Bekannte Einschränkungen

- **Ordner mit Inhalt**: Ordner, die Unterordner oder Dateien enthalten, können nicht über die API gelöscht werden. Verwenden Sie stattdessen die grafische Benutzeroberfläche (GUI) für diese Operation.
- **Datei-Upload**: Die aktuelle API-Version unterstützt keine vollständige Datei-Upload-Funktionalität über die API. Dateien müssen über die GUI hochgeladen werden.

## Sicherheitsüberlegungen

- Die API verwendet eine einfache tokenbasierte Authentifizierung.
- Die Tokens werden im Speicher verwaltet und sind nicht persistent über Neustarts hinweg.
- In einer Produktionsumgebung sollten zusätzliche Sicherheitsmaßnahmen wie HTTPS, Token-Zeitlimits und sichere Passwörter implementiert werden. 