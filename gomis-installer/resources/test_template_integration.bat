@echo off
echo Testing GOMIS Template Integration (64-bit only)...
echo.

set "JAVA_HOME=%~dp0jre"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Java version:
java -version
echo.

echo Testing template directory detection...
echo Expected 64-bit installation path: C:\Program Files\GOMIS\templates\
echo Expected development path: %CD%\templates\
echo.

if exist "C:\Program Files\GOMIS\templates\" (
    echo [FOUND] 64-bit Installation template directory
    dir "C:\Program Files\GOMIS\templates\"
) else (
    echo [NOT FOUND] 64-bit Installation template directory
)

if exist "templates\" (
    echo [FOUND] Development template directory
    dir "templates\"
) else (
    echo [NOT FOUND] Development template directory
)

echo.
echo Testing template files...
set "TEMPLATES_FOUND=0"

if exist "C:\Program Files\GOMIS\templates\dropping_form_template.docx" (
    echo [OK] 64-bit Installation: dropping_form_template.docx
    set /a TEMPLATES_FOUND+=1
) else if exist "templates\dropping_form_template.docx" (
    echo [OK] Development: dropping_form_template.docx
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] dropping_form_template.docx not found
)

if exist "C:\Program Files\GOMIS\templates\incident_report_template.docx" (
    echo [OK] 64-bit Installation: incident_report_template.docx
    set /a TEMPLATES_FOUND+=1
) else if exist "templates\incident_report_template.docx" (
    echo [OK] Development: incident_report_template.docx
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] incident_report_template.docx not found
)

if exist "C:\Program Files\GOMIS\templates\good_moral_template.docx" (
    echo [OK] 64-bit Installation: good_moral_template.docx
    set /a TEMPLATES_FOUND+=1
) else if exist "templates\good_moral_template.docx" (
    echo [OK] Development: good_moral_template.docx
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] good_moral_template.docx not found
)

echo.
echo Summary: %TEMPLATES_FOUND% out of 3 templates found

if %TEMPLATES_FOUND%==3 (
    echo [SUCCESS] All templates are accessible
    echo Template integration is ready for testing
) else (
    echo [WARNING] Some templates are missing
    echo Please ensure templates are properly installed
)

echo.
echo Testing user template directory...
set "USER_TEMPLATE_DIR=%USERPROFILE%\AppData\Roaming\GOMIS\templates"
if exist "%USER_TEMPLATE_DIR%" (
    echo [OK] User template directory exists: %USER_TEMPLATE_DIR%
) else (
    echo [INFO] User template directory will be created on first run: %USER_TEMPLATE_DIR%
)

echo.
echo Note: This application supports both 64-bit and 32-bit Windows systems.
echo.

REM Check for jre directory
if exist "%~dp0jre" (
    echo [OK] jre directory found
) else (
    echo [ERROR] jre directory not found
)

REM Check for GOMIS.exe
if exist "C:\Program Files\GOMIS\GOMIS.exe" (
    echo [OK] GOMIS.exe found
) else (
    echo [ERROR] GOMIS.exe not found
)

pause 