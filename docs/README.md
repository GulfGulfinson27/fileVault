# GitHub Pages für FileVault

Diese Verzeichnis enthält die Dateien für die GitHub Pages Website des FileVault-Projekts.

## Einrichtung von GitHub Pages

Um GitHub Pages für das Repository zu aktivieren:

1. Gehen Sie zu den Repository-Einstellungen auf GitHub
2. Scrollen Sie zum Abschnitt "GitHub Pages"
3. Wählen Sie als Quelle "Deploy from a branch"
4. Wählen Sie den Branch "main" und das Verzeichnis "/docs"
5. Klicken Sie auf "Save"

Ihre Website wird dann unter `https://[username].github.io/fileVault` verfügbar sein.

## Benutzerdefinierte Domain

Wenn Sie eine benutzerdefinierte Domain verwenden möchten:

1. Ändern Sie die CNAME-Datei im docs-Verzeichnis, um Ihre Domain zu spezifizieren
2. Stellen Sie sicher, dass DNS-Einträge korrekt konfiguriert sind
3. Gehen Sie zu den GitHub Pages Einstellungen und geben Sie Ihre benutzerdefinierte Domain ein

## Inhalt

Der Inhalt dieser Website wird automatisch aus dem Markdown-Dateien des Projekts und den statischen HTML-Dateien in diesem Verzeichnis generiert. 

## Docker Support

FileVault bietet jetzt Docker-Unterstützung für die API-Funktionalität. Bitte beachten Sie, dass nur die API im Container funktioniert und das Hauptprojekt weiterhin benötigt wird. Um den Docker Container zu verwenden:

```
docker pull ghcr.io/GulfGulfinson/fileVault:latest
docker run -v ~/.filevault:/root/.filevault -p 9090:9090 ghcr.io/GulfGulfinson/fileVault:latest
```

Die Website wurde aktualisiert, um die Docker-Verfügbarkeit zu reflektieren. 