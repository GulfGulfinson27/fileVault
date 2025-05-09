@echo off

:: Default API port
set PORT=%1
if "%PORT%"=="" set PORT=9090

:: Start the FileVault application with API server
mvn exec:java -Dexec.mainClass="com.filevault.FileVaultApp" -Dexec.args="--api-port=%PORT%" -Dexec.cleanupDaemonThreads=false

pause 