@echo off
echo Preparing files for Launch4j...

:: Create directory structure
mkdir exe-build
cd exe-build

:: Copy application files
copy ..\gomis-app\lib\gomis-0.0.1-alpha.jar lib\
copy ..\gomis-app\bin\jre jre\ /E /I /Y

:: Copy configuration and icon
copy ..\launch4j-config.xml .
copy ..\GOMIS.ico .

:: Create database directory
mkdir database

:: Copy database script
copy ..\gomisDB.sql database\

echo Files prepared successfully!
echo.
echo Next steps:
echo 1. Download Launch4j from https://launch4j.sourceforge.net/
echo 2. Install Launch4j
echo 3. Open launch4j-config.xml in Launch4j
echo 4. Click the build button (wrench icon)
echo 5. The EXE will be created in the exe-build directory
echo.
pause 