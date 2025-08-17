@echo off
setlocal enabledelayedexpansion

REM Set paths
set "LOGFILE=C:\gomisLogs\watchdog.log"
set "GOMIS_PATH=%ProgramFiles%\GOMIS\GOMIS.exe"
set "MAX_RETRIES=3"
set "RETRY_DELAY=10"

REM Configurable MariaDB service name
set "MARIADB_SERVICE=MariaDB"
REM Configurable MariaDB executable path
set "MYSQL_PATH=%ProgramFiles%\MariaDB 11.4\bin\mysql.exe"

REM Create log directory if it doesn't exist
if not exist "C:\gomisLogs" mkdir "C:\gomisLogs"

REM Log function
:log
echo %date% %time% - %~1 >> "%LOGFILE%"
goto :eof

REM Check if script is already running
tasklist /FI "IMAGENAME eq watchdog.bat" | find /I "watchdog.bat" >nul
if not errorlevel 1 (
    call :log "Watchdog already running, exiting"
    exit /b
)

call :log "Watchdog started"

REM Check and start network service if needed
sc query "Netman" | find "RUNNING" >nul
if errorlevel 1 (
    call :log "Starting Network service"
    net start "Netman"
)

REM Start MariaDB service if not running
set "RETRY_COUNT=0"
:check_mariadb
sc query "%MARIADB_SERVICE%" | find "RUNNING" >nul
if errorlevel 1 (
    set /a "RETRY_COUNT+=1"
    if !RETRY_COUNT! gtr %MAX_RETRIES% (
        call :log "Failed to start %MARIADB_SERVICE% after %MAX_RETRIES% attempts"
        exit /b 1
    )
    call :log "Starting %MARIADB_SERVICE% service (Attempt !RETRY_COUNT!)"
    net start "%MARIADB_SERVICE%" >> "%LOGFILE%" 2>&1
    if errorlevel 1 (
        call :log "Failed to start %MARIADB_SERVICE%, retrying in %RETRY_DELAY% seconds"
        timeout /t %RETRY_DELAY% /nobreak >nul
        goto check_mariadb
    )
)

REM Wait for MariaDB to be fully initialized
call :log "Waiting for MariaDB to initialize"
timeout /t 30 /nobreak >nul

REM Check for MariaDB executable
if not exist "%MYSQL_PATH%" (
    call :log "MariaDB executable not found at %MYSQL_PATH%"
    exit /b 1
)

REM Start GOMIS if not running
tasklist /FI "IMAGENAME eq GOMIS.exe" | find /I "GOMIS.exe" >nul
if errorlevel 1 (
    call :log "Starting GOMIS"
    if exist "%GOMIS_PATH%" (
        start "" "%GOMIS_PATH%"
        if errorlevel 1 (
            call :log "Failed to start GOMIS"
            exit /b 1
        )
    ) else (
        call :log "GOMIS executable not found at %GOMIS_PATH%"
        exit /b 1
    )
)

call :log "Watchdog completed successfully"
exit /b 0 