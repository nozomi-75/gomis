@echo off
setlocal enabledelayedexpansion

:: Check if the password argument is provided
if "%~1"=="" (
    echo Error: No password provided! Exiting...
    echo [%DATE% %TIME%] Error: No password provided! >> "C:\gomisLogs\db_install.log"
    exit /b 1
)

:: Check if the app directory argument is provided
if "%~2"=="" (
    echo Error: No app directory provided! Exiting...
    echo [%DATE% %TIME%] Error: No app directory provided! >> "C:\gomisLogs\db_install.log"
    exit /b 1
) else (
    set "APP_DIR=%~2"
)

:: Define paths
set "MYSQL_PATH=C:\Program Files\MariaDB 11.4\bin\mysql.exe"
if not exist "!MYSQL_PATH!" (
    echo [%DATE% %TIME%] Error: MariaDB 64-bit not found at !MYSQL_PATH! >> "C:\gomisLogs\db_install.log"
    echo Error: MariaDB 64-bit not found! Exiting...
    exit /b 1
)
set "DB_USER=root"
set "DB_PASS=%~1"
set "SQL_SCRIPT=%APP_DIR%\database\gomisDB.sql"
set "LOG_FILE=C:\gomisLogs\db_install.log"

:: Debugging information
echo [%DATE% %TIME%] Starting GOMIS Database Setup Script >> "!LOG_FILE!"
echo [%DATE% %TIME%] App directory set to: !APP_DIR! >> "!LOG_FILE!"
echo [%DATE% %TIME%] SQL script location: !SQL_SCRIPT! >> "!LOG_FILE!"
echo [%DATE% %TIME%] MySQL/MariaDB path: !MYSQL_PATH! >> "!LOG_FILE!"

:: Check if the SQL script exists
if not exist "!SQL_SCRIPT!" (
    echo [%DATE% %TIME%] Error: SQL script file not found at !SQL_SCRIPT! >> "!LOG_FILE!"
    echo [%DATE% %TIME%] Listing directory contents: >> "!LOG_FILE!"
    dir "!APP_DIR!\database" >> "!LOG_FILE!" 2>&1
    exit /b 1
)

:: Wait for MariaDB service to be fully running
echo [%DATE% %TIME%] Waiting for MariaDB service to start... >> "!LOG_FILE!"
for /L %%i in (1,1,12) do (
    sc query MariaDB | find "RUNNING" >nul && goto :startSQL
    echo [%DATE% %TIME%] MariaDB not running yet, waiting... >> "!LOG_FILE!"
    timeout /t 5 >nul
)
echo [%DATE% %TIME%] Error: MariaDB service did not start! >> "!LOG_FILE!"
echo Error: MariaDB service did not start! Check logs.
exit /b 1

:startSQL
:: Create the database
echo [%DATE% %TIME%] Creating database 'gomisdb'... >> "!LOG_FILE!"
"!MYSQL_PATH!" -u !DB_USER! -p!DB_PASS! --execute="CREATE DATABASE IF NOT EXISTS gomisdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" >> "!LOG_FILE!" 2>&1
if %errorlevel% neq 0 (
    echo [%DATE% %TIME%] Error: Failed to create database 'gomisdb' >> "!LOG_FILE!"
    exit /b 1
)

:: Execute the SQL script
echo [%DATE% %TIME%] Running SQL script... >> "!LOG_FILE!"
"!MYSQL_PATH!" -u !DB_USER! -p!DB_PASS! gomisdb < "!SQL_SCRIPT!" >> "!LOG_FILE!" 2>&1
if %errorlevel% neq 0 (
    echo [%DATE% %TIME%] Error: SQL script execution failed >> "!LOG_FILE!"
    exit /b 1
)

:: Verify database creation
echo [%DATE% %TIME%] Checking if database exists... >> "!LOG_FILE!"
"!MYSQL_PATH!" -u !DB_USER! -p!DB_PASS! --execute="SHOW DATABASES;" | findstr /I "gomisdb" >nul
if %errorlevel% neq 0 (
    echo [%DATE% %TIME%] Error: Database 'gomisdb' was not created! >> "!LOG_FILE!"
    echo Error: Database 'gomisdb' was not created! Check SQL script.
    exit /b 1
)

echo [%DATE% %TIME%] Database script executed successfully. >> "!LOG_FILE!"
echo Database script executed successfully.
exit /b 0 