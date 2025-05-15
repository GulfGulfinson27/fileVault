@echo off
setlocal

REM Default API port
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

REM Start the FileVault application with API server
java -jar "%JAR_PATH%" --api-port=%PORT%

pause 