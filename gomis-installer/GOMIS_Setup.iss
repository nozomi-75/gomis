[Setup]
AppName=GOMIS
AppVersion=0.0.1-alpha
DefaultDirName={autopf}\GOMIS
DefaultGroupName=GOMIS
OutputDir=Output
OutputBaseFilename=GOMIS_Installer
Compression=lzma2
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile=GOMIS.ico
DisableWelcomePage=no
DisableFinishedPage=no
WizardStyle=modern
AppPublisher=GOMIS Team
AppCopyright=© 2025 GOMIS Team
UninstallDisplayIcon={app}\GOMIS.ico
ShowLanguageDialog=yes
MinVersion=0,6.1
ArchitecturesInstallIn64BitMode=x64

[Files]
Source: "mariadb-11.4.5-winx64.msi"; DestDir: "{tmp}"; Flags: ignoreversion deleteafterinstall
Source: "gomisDB.sql"; DestDir: "{app}\database"; Flags: ignoreversion
Source: "exe-build\GOMIS.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "GOMIS.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "execute_sql.bat"; DestDir: "{tmp}"; Flags: ignoreversion deleteafterinstall
Source: "exe-build\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs createallsubdirs

[Dirs]
Name: "{app}\database"; Flags: uninsalwaysuninstall
Name: "C:\gomisLogs"; Flags: uninsneveruninstall

[Run]
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\mariadb-11.4.5-winx64.msi"" /qn /norestart SERVICENAME=""MariaDB"" PASSWORD=""{code:GetMariaDBPassword}"" ENABLESERVICESTARTUP=1 /l*v ""C:\gomisLogs\mariadb_install.log"" ADDLOCAL=ALL REMOVE=HeidiSQL"; StatusMsg: "Installing MariaDB Server..."; Flags: waituntilterminated runasoriginaluser; BeforeInstall: CheckMariaDBInstalled
Filename: "net.exe"; Parameters: "start MariaDB"; StatusMsg: "Starting MariaDB service..."; Flags: runhidden waituntilterminated shellexec; Check: ServiceExists('MariaDB') and not ServiceRunning('MariaDB')
Filename: "cmd.exe"; Parameters: "/c timeout /t 30 >nul"; StatusMsg: "Waiting for MariaDB service..."; Flags: runhidden waituntilterminated
Filename: "cmd.exe"; Parameters: "/c ""{code:GetMariaDBExePath}"" -u root -p""{code:GetMariaDBPassword}"" --execute=""SELECT 'Connection successful' AS Status;"" > ""C:\gomisLogs\connection_test.log"" 2>&1"; StatusMsg: "Testing database connection..."; Flags: runhidden waituntilterminated; Check: FileExists('{code:GetMariaDBExePath}')
Filename: "cmd.exe"; Parameters: "/c timeout /t 10 >nul"; StatusMsg: "Waiting for MariaDB to be fully initialized..."; Flags: runhidden waituntilterminated
Filename: "{tmp}\execute_sql.bat"; Parameters: """{code:GetMariaDBPassword}"" ""{app}"""; StatusMsg: "Setting up GOMIS database..."; Flags: runasoriginaluser waituntilterminated
Filename: "{app}\GOMIS.exe"; Description: "Launch GOMIS now"; Flags: postinstall nowait skipifsilent unchecked

[Icons]
Name: "{group}\GOMIS"; Filename: "{app}\GOMIS.exe"; WorkingDir: "{app}"; IconFilename: "{app}\GOMIS.ico"; Comment: "Launch GOMIS Application"
Name: "{commondesktop}\GOMIS"; Filename: "{app}\GOMIS.exe"; WorkingDir: "{app}"; IconFilename: "{app}\GOMIS.ico"; Comment: "Launch GOMIS Application"
Name: "{group}\Uninstall GOMIS"; Filename: "{uninstallexe}"; Comment: "Uninstall GOMIS"

[UninstallRun]
Filename: "net.exe"; Parameters: "stop MariaDB"; Flags: runhidden; Check: ServiceExists('MariaDB')

[UninstallDelete]
Type: filesandordirs; Name: "{app}\database"
Type: files; Name: "{app}\*.*"

[Code]
var
  MariaDBPassword: string;
  DBUserPage: TInputQueryWizardPage;
  InstallSuccess: Boolean;
  ShowPasswordBtn: TNewButton;
  PasswordEdit: TPasswordEdit;
  ErrorCode: Integer;

procedure ShowPasswordClick(Sender: TObject);
begin
  if PasswordEdit.Password then
  begin
    PasswordEdit.Password := False;
    ShowPasswordBtn.Caption := 'Hide';
  end
  else
  begin
    PasswordEdit.Password := True;
    ShowPasswordBtn.Caption := 'Show';
  end;
end;

procedure InitializeWizard;
begin
  DBUserPage := CreateInputQueryPage(wpWelcome, 'Database Configuration', 'Setup will configure MariaDB server with the following credentials:', 'Please specify the root password for MariaDB:');

  PasswordEdit := TPasswordEdit.Create(DBUserPage);
  PasswordEdit.Parent := DBUserPage.Surface;
  PasswordEdit.Top := ScaleY(40);
  PasswordEdit.Left := ScaleX(0);
  PasswordEdit.Width := DBUserPage.SurfaceWidth - ScaleX(60);
  PasswordEdit.Text := '';

  ShowPasswordBtn := TNewButton.Create(DBUserPage);
  ShowPasswordBtn.Parent := DBUserPage.Surface;
  ShowPasswordBtn.Top := ScaleY(38);
  ShowPasswordBtn.Left := PasswordEdit.Left + PasswordEdit.Width + ScaleX(5);
  ShowPasswordBtn.Width := ScaleX(50);
  ShowPasswordBtn.Height := ScaleY(23);
  ShowPasswordBtn.Caption := 'Show';
  ShowPasswordBtn.OnClick := @ShowPasswordClick;

  InstallSuccess := True;
end;

function GetMariaDBPassword(Param: string): string;
begin
  Result := PasswordEdit.Text;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  if CurPageID = DBUserPage.ID then
  begin
    if PasswordEdit.Text = '' then
    begin
      MsgBox('Please enter a valid password for MariaDB.', mbError, MB_OK);
      Result := False;
    end
    else if Length(PasswordEdit.Text) < 8 then
    begin
      MsgBox('Password must be at least 8 characters long.', mbError, MB_OK);
      Result := False;
    end
    else if Pos(' ', PasswordEdit.Text) > 0 then
    begin
      MsgBox('Password cannot contain spaces.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

function GetMariaDBExePath(Param: string): string;
begin
  Result := '';
  if FileExists('C:\Program Files\MariaDB 11.4\bin\mysql.exe') then
    Result := 'C:\Program Files\MariaDB 11.4\bin\mysql.exe'
  else if FileExists('C:\Program Files\MariaDB 11.4\bin\mariadb.exe') then
    Result := 'C:\Program Files\MariaDB 11.4\bin\mariadb.exe'
  else if FileExists('C:\Program Files (x86)\MariaDB 11.4\bin\mysql.exe') then
    Result := 'C:\Program Files (x86)\MariaDB 11.4\bin\mysql.exe'
  else if FileExists('C:\Program Files (x86)\MariaDB 11.4\bin\mariadb.exe') then
    Result := 'C:\Program Files (x86)\MariaDB 11.4\bin\mariadb.exe'
  else
    Result := '';
end;

function ServiceExists(ServiceName: string): Boolean;
var
  ResultCode: Integer;
begin
  Exec('sc.exe', 'query ' + ServiceName, '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  Result := (ResultCode = 0);
end;

function ServiceRunning(ServiceName: string): Boolean;
var
  ResultCode: Integer;
begin
  Exec('sc.exe', 'query ' + ServiceName + ' | find "RUNNING"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  Result := (ResultCode = 0);
end;

procedure CheckMariaDBInstalled();
var
  MariaDBPath: string;
begin
  MariaDBPath := GetMariaDBExePath('');
  if FileExists(MariaDBPath) then
  begin
    if MsgBox('MariaDB 11.4 appears to be installed already. Do you want to reinstall it?', mbConfirmation, MB_YESNO) = IDNO then
    begin
      if not ServiceRunning('MariaDB') then
        Exec('net.exe', 'start MariaDB', '', SW_HIDE, ewWaitUntilTerminated, ErrorCode);
    end;
  end;
end;

procedure LoadStringFromFile(const FileName: String; var Content: String);
var
  Buffer: AnsiString;
  FileStream: TFileStream;
begin
  if FileExists(FileName) then
  begin
    FileStream := TFileStream.Create(FileName, fmOpenRead or fmShareDenyWrite);
    try
      SetLength(Buffer, FileStream.Size);
      FileStream.ReadBuffer(Buffer[1], FileStream.Size);
      Content := Buffer;
    finally
      FileStream.Free;
    end;
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  ConnectionLogFile, InstallLogFile, ConnectionLogContent: String;
begin
  if CurStep = ssPostInstall then
  begin
    InstallLogFile := 'C:\gomisLogs\mariadb_install.log';
    ConnectionLogFile := 'C:\gomisLogs\connection_test.log';

    if not FileExists(GetMariaDBExePath('')) then
    begin
      InstallSuccess := False;
      if FileExists(InstallLogFile) then
        MsgBox('MariaDB installation failed. Check log: ' + InstallLogFile, mbError, MB_OK)
      else
        MsgBox('MariaDB installation failed.', mbError, MB_OK);
    end
    else if FileExists(ConnectionLogFile) then
    begin
      LoadStringFromFile(ConnectionLogFile, ConnectionLogContent);
      if Pos('Connection successful', ConnectionLogContent) = 0 then
      begin
        InstallSuccess := False;
        MsgBox('Failed to connect to MariaDB. Please check your password and try again.', mbError, MB_OK);
      end;
    end
    else if not FileExists(ExpandConstant('{app}\database\gomisDB.sql')) then
    begin
      InstallSuccess := False;
      MsgBox('SQL script file was not found. Database setup may be incomplete.', mbError, MB_OK);
    end;
  end;

  if CurStep = ssDone then
  begin
    if InstallSuccess and WizardForm.RunList.Checked[0] then
      Exec(ExpandConstant('{app}\GOMIS.exe'), '', ExpandConstant('{app}'), SW_SHOW, ewNoWait, ErrorCode);
  end;
end;

function InstallSuccessful(): Boolean;
begin
  Result := InstallSuccess;
end; 