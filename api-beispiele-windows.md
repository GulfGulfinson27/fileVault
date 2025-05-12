# FileVault API Beispiele für Windows (PowerShell Invoke-WebRequest)

Dieses Dokument enthält Beispiele für die Verwendung der FileVault API mit PowerShell's `Invoke-WebRequest` unter Windows.

## Basiseinstellung

Die folgenden Beispiele gehen davon aus, dass der FileVault API-Server unter `localhost:9090` läuft. Falls Sie einen anderen Host oder Port verwenden, passen Sie die Befehle entsprechend an.

## Authentifizierung

### Bei der API anmelden

```powershell
$body = @{
    password = "11111111"
} | ConvertTo-Json

$authResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/auth" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

$authData = $authResponse.Content | ConvertFrom-Json

Write-Host "Token: $($authData.token)"
```

**Erwartete Antwort**:

```
Token: d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8
```

## Ordnerverwaltung

### Alle Ordner auflisten

```powershell
$headers = @{
    Authorization = "d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
}

$foldersResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/folders" `
    -Method GET `
    -Headers $headers

$folders = $foldersResponse.Content | ConvertFrom-Json
$folders | Format-Table
```

**Erwartete Ausgabe**:

```
id name         parentFolderId
-- ----         -------------
 1 Persönlich               0
 2 Arbeit                   0
 3 Dokumente                1
```

### Neuen Ordner erstellen

#### Ordner in der Root-Ebene erstellen

```powershell
$headers = @{
    Authorization = "d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
}

$newFolderBody = @{
    name = "Neuer Ordner"
} | ConvertTo-Json

$createFolderResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/folders" `
    -Method POST `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $newFolderBody

$newFolder = $createFolderResponse.Content | ConvertFrom-Json
$newFolder
```

#### Ordner innerhalb eines vorhandenen Ordners erstellen

```powershell
$headers = @{
    Authorization = "d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
}

$newSubfolderBody = @{
    name = "Unterordner"
    parentFolderId = 1
} | ConvertTo-Json

$createSubfolderResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/folders" `
    -Method POST `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $newSubfolderBody

$newSubfolder = $createSubfolderResponse.Content | ConvertFrom-Json
$newSubfolder
```

**Erwartete Ausgabe**:

```
id name        parentFolderId
-- ----        -------------
 4 Unterordner             1
```

### Ordnernamen aktualisieren

```powershell
$headers = @{
    Authorization = "d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
}

$updateFolderBody = @{
    id = 4
    name = "Umbenannter Ordner"
} | ConvertTo-Json

$updateFolderResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/folders" `
    -Method PUT `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $updateFolderBody

$updatedFolder = $updateFolderResponse.Content | ConvertFrom-Json
$updatedFolder
```

**Erwartete Ausgabe**:

```
id name               parentFolderId
-- ----               -------------
 4 Umbenannter Ordner             1
```

### Ordner löschen

```powershell
$headers = @{
    Authorization = "d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
}

$deleteFolderResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/folders?id=4" `
    -Method DELETE `
    -Headers $headers

$deleteFolderResponse.Content
```

**Erwartete Ausgabe**:

```
Ordner erfolgreich gelöscht.
```

**Mögliche Fehlermeldung (bei Ordner mit Inhalt)**:

```
Ordner mit Inhalt können nicht über die API gelöscht werden. Bitte verwenden Sie die grafische Benutzeroberfläche (GUI), um Ordner mit Unterordnern oder Dateien zu löschen.
```

## Dateiverwaltung

### Alle Dateien auflisten

```powershell
$headers = @{
    Authorization = "d123e4a4-5b6c-78d9-0e1f-2g3h4i56j7k8"
}

$filesResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/files" `
    -Method GET `
    -Headers $headers

$files = $filesResponse.Content | ConvertFrom-Json
$files | Format-Table
```

**Erwartete Ausgabe**:

```
id name         folderId
-- ----         --------
 1 bericht.pdf         2
 2 notizen.txt         1
```

## Fehlerbehandlung

### Beispiel für ungültige Authentifizierung

```powershell
$wrongPasswordBody = @{
    password = "falschespasswort"
} | ConvertTo-Json

try {
    $authResponse = Invoke-WebRequest -Uri "http://localhost:9090/api/auth" `
        -Method POST `
        -ContentType "application/json" `
        -Body $wrongPasswordBody
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Fehler: $($errorResponse.error)"
}
```

**Erwartete Ausgabe**:

```
Fehler: Ungültiges Passwort. Zugriff verweigert.
```

### Beispiel für eine Anfrage ohne Token

```powershell
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/api/folders" -Method GET
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Fehler: $($errorResponse.error)"
}
```

**Erwartete Ausgabe**:

```
Fehler: Unauthorized: Invalid or missing authentication token.
```

### Beispiel für eine Anfrage mit ungültigem Token

```powershell
$invalidHeaders = @{
    Authorization = "ungültigestoken"
}

try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/api/folders" `
        -Method GET `
        -Headers $invalidHeaders
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "Fehler: $($errorResponse.error)"
}
```

**Erwartete Ausgabe**:

```
Fehler: Unauthorized: Invalid or missing authentication token.
```

## Testen des Web-Interfaces

Sie können auch auf das Web-Interface zugreifen, um die API interaktiv zu testen:

```powershell
$webInterfaceResponse = Invoke-WebRequest -Uri "http://localhost:9090/" -Method GET
$html = $webInterfaceResponse.Content

# Speichern Sie das HTML in einer Datei und öffnen Sie es im Browser
$html | Out-File -FilePath "filevault-web-interface.html"
Start-Process "filevault-web-interface.html"
```

Dadurch wird das Web-Interface im Standardbrowser geöffnet, wo Sie die API interaktiv testen können. 