# GOMIS Installer

This directory contains all the necessary files to create a Windows installer for the GOMIS application.

## Directory Structure

```
gomis-installer/
├── launch4j-config.xml    # Launch4j configuration for creating EXE
├── prepare-exe.bat        # Script to prepare files for Launch4j
├── GOMIS_Setup.iss        # Inno Setup script for creating installer
├── GOMIS.ico             # Application icon
├── mariadb-11.4.5-winx64.msi  # MariaDB installer
├── gomisDB.sql           # Database initialization script
└── execute_sql.bat       # Script to execute SQL commands
```

## Prerequisites

1. Launch4j (https://launch4j.sourceforge.net/)
2. Inno Setup (https://jrsoftware.org/isdl.php)
3. MariaDB 11.4.5 MSI installer
4. Application icon (GOMIS.ico)
5. Database initialization script (gomisDB.sql)

## Build Process

1. Prepare the application files:
   ```batch
   prepare-exe.bat
   ```

2. Create the EXE using Launch4j:
   - Open launch4j-config.xml in Launch4j
   - Click the build button (wrench icon)
   - The EXE will be created in the exe-build directory

3. Create the installer using Inno Setup:
   - Open GOMIS_Setup.iss in Inno Setup Compiler
   - Click Build > Compile
   - The installer will be created in the Output directory

## Required Files

Make sure these files are present before building:
- mariadb-11.4.5-winx64.msi
- GOMIS.ico
- gomisDB.sql
- execute_sql.bat

## Database Setup

The installer includes an improved database setup process that:
- Creates a UTF-8 encoded database named 'gomisdb'
- Waits for MariaDB service to be fully running
- Provides detailed logging in C:\gomisLogs\db_install.log
- Verifies database creation and script execution
- Handles both 32-bit and 64-bit MariaDB installations

## Notes

- The installer requires administrator privileges
- MariaDB will be installed as a Windows service
- The application uses a custom JRE bundled with the installer
- Database credentials are configured during installation
- Logs are stored in C:\gomisLogs
- Database setup logs are in C:\gomisLogs\db_install.log

## Troubleshooting

1. If the EXE creation fails:
   - Verify Launch4j is properly installed
   - Check if all required files are present
   - Ensure the JRE path is correct in launch4j-config.xml

2. If the installer creation fails:
   - Verify Inno Setup is properly installed
   - Check if all required files are present
   - Ensure file paths in GOMIS_Setup.iss are correct

3. If database setup fails:
   - Check database installation logs in C:\gomisLogs\db_install.log
   - Verify database credentials
   - Ensure MariaDB service is running
   - Check if the SQL script exists in the correct location
   - Verify MariaDB installation path (32-bit or 64-bit) 