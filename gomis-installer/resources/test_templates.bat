@echo off
echo Testing template files for GOMIS installer...
echo.

set "TEMPLATE_DIR=..\TEMPLATES"
set "TEMPLATES_FOUND=0"

echo Checking template directory: %TEMPLATE_DIR%
if exist "%TEMPLATE_DIR%" (
    echo [OK] Template directory exists
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] Template directory not found
)

echo.
echo Checking individual template files:

if exist "%TEMPLATE_DIR%\dropping_form_template.docx" (
    echo [OK] dropping_form_template.docx found
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] dropping_form_template.docx not found
)

if exist "%TEMPLATE_DIR%\incident_report_template.docx" (
    echo [OK] incident_report_template.docx found
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] incident_report_template.docx not found
)

if exist "%TEMPLATE_DIR%\good_moral_template.docx" (
    echo [OK] good_moral_template.docx found
    set /a TEMPLATES_FOUND+=1
) else (
    echo [ERROR] good_moral_template.docx not found
)

REM Check file sizes to ensure files are not empty/corrupted
for %%F in (dropping_form_template.docx incident_report_template.docx good_moral_template.docx) do (
    if exist "%TEMPLATE_DIR%\%%F" (
        for %%S in ("%TEMPLATE_DIR%\%%F") do (
            if %%~zS lss 1024 (
                echo [WARNING] %%F is smaller than 1KB, may be corrupted
            )
        )
    )
)

echo.
echo Summary: %TEMPLATES_FOUND% out of 4 checks passed

if %TEMPLATES_FOUND%==4 (
    echo [SUCCESS] All templates are ready for installer
    echo You can now build the installer with Inno Setup
) else (
    echo [WARNING] Some templates are missing or may be corrupted
    echo Please ensure all template files are in the TEMPLATES directory
)

echo.
pause 