# GitHub-Konfiguration für FileVault

Dieses Verzeichnis enthält alle GitHub-spezifischen Konfigurationen und Workflows für das FileVault-Projekt.

## Workflows

Die folgenden GitHub Actions Workflows sind im `workflows/` Verzeichnis definiert:

- `build.yml`: Baut das Projekt und führt Tests aus
- `release.yml`: Erstellt Releases und generiert Artefakte
- `publish-package.yml`: Veröffentlicht das Maven-Paket in GitHub Packages
- `container-scan.yml`: Führt Sicherheitsscans für Container durch
- `sonarcloud.yml`: Führt Code-Qualitätsanalysen mit SonarCloud durch
- `code-quality.yml`: Führt statische Codeanalysen durch
- `github-pages.yml`: Baut und veröffentlicht die Projektwebsite

## GitHub Pages

Die Projektwebsite wird automatisch aus den Dokumentationsdateien im Repository erstellt und unter [https://GulfGulfinson.github.io/fileVault](https://GulfGulfinson.github.io/fileVault) bereitgestellt. Der Inhalt wird aus folgenden Quellen generiert:

- README.md im Hauptverzeichnis
- Markdown-Dateien im src/-Verzeichnis
- HTML-Dateien im docs/-Verzeichnis
- JavaDoc-Dokumentation aus dem Quellcode

## Dependabot

Dependabot ist konfiguriert, um Abhängigkeiten automatisch zu aktualisieren und Sicherheitsupdates zu liefern. Die Konfiguration befindet sich in der `dependabot.yml` Datei.

## Konfigurationsänderungen

Wenn Sie Änderungen an den GitHub-Workflows oder anderen GitHub-Konfigurationen vornehmen möchten, erstellen Sie bitte einen Pull Request mit einer detaillierten Beschreibung der Änderungen. 