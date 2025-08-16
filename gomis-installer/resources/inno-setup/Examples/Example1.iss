; GOMIS Installer Script for Inno Setup
[Setup]
AppName=GOMIS (Guidance Office Management Information System)
AppVersion=1.0
DefaultDirName={pf}\GOMIS
DefaultGroupName=GOMIS
OutputDir=.
OutputBaseFilename=GOMIS_Installer
Compression=lzma2
SolidCompression=yes

[Files]
; MySQL Server Installer (Make sure to rename it to match the actual file)
Source: "mysql-installer.msi"; DestDir: "{tmp}"; Flags: deleteafterinstall
; Database SQL Script
Source: "gomisDB.sql"; DestDir: "{tmp}"; Flags: deleteafterinstall
; Java Runtime Environment (JRE)
Source: "jre-8u441-windows-i586"; DestDir: "{tmp}"; Flags: deleteafterinstall
; GOMIS Executable (Converted from JAR)
Source: "GOMIS.exe"; DestDir: "{app}"; Flags: ignoreversion

[Run]
; Step 1: Install MySQL Server silently
Filename: "{tmp}\mysql-installer.msi"; Parameters: "/quiet"; Flags: waituntilterminated

; Step 2: Wait for MySQL Service to start
Filename: "net"; Parameters: "start MySQL"; Flags: runhidden waituntilterminated

; Step 3: Run SQL script to set up database
Filename: "mysql"; Parameters: "-u root -prootpassword < {tmp}\database.sql"; Flags: runhidden waituntilterminated

; Step 4: Install Java Runtime Environment (JRE) silently
Filename: "{tmp}\jre-8u441-windows-i586"; Parameters: "/s"; Flags: waituntilterminated

; Step 5: Install GOMIS System
Filename: "{app}\GOMIS.exe"; Flags: postinstall nowait

; Step 6: Show completion message
Filename: "cmd"; Parameters: "/c echo GOMIS Installation Complete! && pause"; Flags: runhidden

[Icons]
Name: "{group}\GOMIS"; Filename: "{app}\GOMIS.exe"; WorkingDir: "{app}"

[UninstallRun]
; Stop MySQL service before uninstalling
Filename: "net"; Parameters: "stop MySQL"; Flags: runhidden
