@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2 (Improved)
@REM
@REM Optional ENV vars:
@REM   MVNW_REPOURL        - Repository URL base for downloading Maven distribution.
@REM   MVNW_USERNAME       - Username for downloading Maven from a secured repository.
@REM   MVNW_PASSWORD       - Password for downloading Maven from a secured repository.
@REM   MVNW_VERBOSE        - "true" to enable verbose logging, otherwise silent.
@REM   MAVEN_USER_HOME     - Override the default user home directory for Maven.
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "__MVNW_ARG0_NAME__=%~nx0"
set "__MVNW_CMD__="
set "__MVNW_ERROR__="
set "__MVNW_PSMODULEP_SAVE__=%PSModulePath%"
set "PSModulePath="

for /f "usebackq tokens=1* delims==" %%A in (`powershell -noprofile -command "& { $scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; try { icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope } catch { Write-Error $_.Exception.Message; exit 1 } }"`) do (
    if "%%A"=="MVN_CMD" (set "__MVNW_CMD__=%%B") else if "%%B"=="" (echo %%A) else (echo %%A=%%B)
)

set "PSModulePath=%__MVNW_PSMODULEP_SAVE__%"
set "__MVNW_PSMODULEP_SAVE__="
set "__MVNW_ARG0_NAME__="
set "MVNW_USERNAME="
set "MVNW_PASSWORD="

if not "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*) else (
    echo Cannot start Maven from wrapper >&2
    exit /b 1
)
goto :EOF

: end batch / begin powershell #>

$ErrorActionPreference = "Stop"
if ($env:MVNW_VERBOSE -eq "true") {
    $VerbosePreference = "Continue"
}

# Calculate distributionUrl from maven-wrapper.properties
$properties = Get-Content -Raw "$scriptDir/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData
$distributionUrl = $properties.distributionUrl
if (!$distributionUrl) {
    Write-Error "Cannot read distributionUrl property in $scriptDir/.mvn/wrapper/maven-wrapper.properties"
}

# Determine if using mvnd and set appropriate variables
switch -Wildcard -CaseSensitive ($distributionUrl -replace '^.*/','') {
    "maven-mvnd-*" {
        $USE_MVND = $true
        $distributionUrl = $distributionUrl -replace '-bin\.[^.]*$',"-windows-amd64.zip"
        $MVN_CMD = "mvnd.cmd"
        break
    }
    default {
        $USE_MVND = $false
        $MVN_CMD = $script -replace '^mvnw','mvn'
        break
    }
}

# Apply MVNW_REPOURL and calculate MAVEN_HOME
if ($env:MVNW_REPOURL) {
    $repoPattern = if ($USE_MVND) { "/org/apache/maven/" } else { "/maven/mvnd/" }
    $distributionUrl = "$env:MVNW_REPOURL$repoPattern$($distributionUrl -replace '^.*'+$repoPattern,'')"
}
$distributionUrlName = $distributionUrl -replace '^.*/',''
$distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''
$mavenHomeParent = if ($env:MAVEN_USER_HOME) { "$env:MAVEN_USER_HOME/wrapper/dists/$distributionUrlNameMain" } else { "$HOME/.m2/wrapper/dists/$distributionUrlNameMain" }
$mavenHomeName = ([System.Security.Cryptography.MD5]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object {$_.ToString("x2")}) -join ''
$mavenHome = "$mavenHomeParent/$mavenHomeName"

if (Test-Path -Path $mavenHome -PathType Container) {
    Write-Verbose "Found existing MAVEN_HOME at $mavenHome"
    Write-Output "MVN_CMD=$mavenHome/bin/$MVN_CMD"
    exit $LASTEXITCODE
}

if (!$distributionUrlNameMain -or ($distributionUrlName -eq $distributionUrlNameMain)) {
    Write-Error "distributionUrl is invalid, must end with *-bin.zip, but found $distributionUrl"
}

# Prepare temporary download directory
$tmpDownloadDirHolder = New-TemporaryFile
$tmpDownloadDir = New-Item -ItemType Directory -Path "$tmpDownloadDirHolder.dir"
$tmpDownloadDirHolder.Delete() | Out-Null
trap {
    if ($tmpDownloadDir.Exists) {
        try { Remove-Item $tmpDownloadDir -Recurse -Force -ErrorAction SilentlyContinue } catch { Write-Warning "Cannot remove $tmpDownloadDir" }
    }
}

New-Item -ItemType Directory -Path $mavenHomeParent -Force | Out-Null

# Download and Install Apache Maven
Write-Verbose "Couldn't find MAVEN_HOME, downloading and installing it ..."
Write-Verbose "Downloading from: $distributionUrl"
Write-Verbose "Downloading to: $tmpDownloadDir/$distributionUrlName"

$webClient = New-Object System.Net.WebClient
if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) {
    $webClient.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD)
}
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$webClient.DownloadFile($distributionUrl, "$tmpDownloadDir/$distributionUrlName")

# Validate SHA-256 checksum if provided
if ($properties.distributionSha256Sum) {
    if ($USE_MVND) {
        Write-Error "Checksum validation is not supported for maven-mvnd. Please disable validation by removing 'distributionSha256Sum' from maven-wrapper.properties."
    }
    Import-Module Microsoft.PowerShell.Utility -Function Get-FileHash
    if ((Get-FileHash "$tmpDownloadDir/$distributionUrlName" -Algorithm SHA256).Hash.ToLower() -ne $properties.distributionSha256Sum) {
        Write-Error "Error: Failed to validate Maven distribution SHA-256. Distribution might be compromised or checksum needs update."
    }
}

# Unzip and move Maven distribution
Expand-Archive "$tmpDownloadDir/$distributionUrlName" -DestinationPath $tmpDownloadDir
Rename-Item -Path "$tmpDownloadDir/$distributionUrlNameMain" -NewName $mavenHomeName
try {
    Move-Item -Path "$tmpDownloadDir/$mavenHomeName" -Destination $mavenHomeParent
} catch {
    if (!(Test-Path -Path $mavenHome -PathType Container)) {
        Write-Error "Failed to move MAVEN_HOME"
    }
} finally {
    try { Remove-Item $tmpDownloadDir -Recurse -Force -ErrorAction SilentlyContinue } catch { Write-Warning "Cannot remove $tmpDownloadDir" }
}

Write-Output "MVN_CMD=$mavenHome/bin/$MVN_CMD"