@echo off

:: Standardport, falls keiner angegeben wird
set PORT=%1
if "%PORT%"=="" set PORT=9090

:: Wechsel in das Projektverzeichnis und starte den Server mit Maven
cd /d "%~dp0" || exit /b
mvn exec:java -Dexec.mainClass="com.filevault.api.FileVaultApiApp" -Dexec.args="%PORT%" -Dexec.cleanupDaemonThreads=false -q