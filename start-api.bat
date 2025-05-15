@echo off
setlocal

REM Standardport, falls keiner angegeben wird
set PORT=9090
if not "%~1"=="" set PORT=%~1

REM Prüfen, ob die JAR-Datei existiert
if exist "target\FileVault-shaded.jar" (
    set JAR_PATH=target\FileVault-shaded.jar
) else if exist "FileVault-shaded.jar" (
    set JAR_PATH=FileVault-shaded.jar
) else if exist "FileVault.jar" (
    set JAR_PATH=FileVault.jar
) else (
    echo Fehler: Keine FileVault JAR-Datei gefunden.
    exit /b 1
)

REM Starte nur die API ohne GUI
java -Djava.awt.headless=true -jar "%JAR_PATH%" --api-port=%PORT%

REM Falls Sie die Konsole offen halten möchten, entkommentieren Sie die folgende Zeile
REM pause