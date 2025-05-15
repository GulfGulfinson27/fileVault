@echo off
setlocal

set "JAVA_HOME=%JAVA_HOME%"
if "%JAVA_HOME%"=="" (
    echo JAVA_HOME ist nicht gesetzt. Bitte setzen Sie JAVA_HOME auf Ihr JDK-Verzeichnis.
    exit /b 1
)

set "JAVAFX_LIB=lib\javafx-sdk-17.0.14\lib"
set "JAVAFX_MODULES=javafx.controls,javafx.fxml"

if not exist "%JAVAFX_LIB%" (
    echo JavaFX-Bibliotheken nicht gefunden in %JAVAFX_LIB%
    exit /b 1
)

echo Starte FileVault...
"%JAVA_HOME%\bin\java" ^
    --module-path "%JAVAFX_LIB%" ^
    --add-modules %JAVAFX_MODULES% ^
    -jar target\FileVault-shaded.jar

if errorlevel 1 (
    echo Fehler beim Starten der Anwendung.
    exit /b 1
) 