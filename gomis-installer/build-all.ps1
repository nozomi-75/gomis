# GOMIS Build Script
# Builds main project and validator, copies validator JAR to resources/output, runs Launch4j to create GOMIS.exe, logs all steps

param(
    [switch]$SkipInno = $false,
    [switch]$OnlyInno = $false
)

$ErrorActionPreference = 'Stop'
$logFile = "$(Join-Path $PSScriptRoot 'build-all.log')"

# Delete old log before starting a new build
if (Test-Path $logFile) { Remove-Item $logFile -Force }

function Log {
    param([string]$msg)
    $timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    "$timestamp $msg" | Tee-Object -FilePath $logFile -Append
}

function Step {
    param([string]$msg)
    Write-Host "`n==== $msg ====" -ForegroundColor Cyan
    Log $msg
}

function Fail {
    param([string]$msg)
    Write-Host "ERROR: $msg" -ForegroundColor Red
    Log "FAIL: $msg"
    exit 1
}

# Paths
$mainDir = Resolve-Path (Join-Path $PSScriptRoot "..\")
$validatorDir = Resolve-Path (Join-Path $PSScriptRoot "..\gomis-account-validator")
$outputDir = Join-Path $PSScriptRoot "resources/output"
$outputInstallerDir = Join-Path $PSScriptRoot "Output"

# Try both possible Launch4j locations
$launch4jExe1 = Join-Path $PSScriptRoot 'resources/launch4j/launch4jc.exe'
$launch4jExe2 = Join-Path $PSScriptRoot 'launch4j/launch4jc.exe'
$launch4jExe = if (Test-Path $launch4jExe1) { $launch4jExe1 } elseif (Test-Path $launch4jExe2) { $launch4jExe2 } else { $null }

# Try both possible config locations
$launch4jConfig1 = Join-Path $PSScriptRoot 'resources/launch4j-config.xml'
$launch4jConfig = if (Test-Path $launch4jConfig1) { $launch4jConfig1 } else { $null }

$innoExe = "resources/inno-setup/ISCC.exe"
$innoScript = "resources/GOMIS_INNO_SETUP.iss"

# Ensure output directory exists
if (-not (Test-Path $outputDir)) { New-Item -ItemType Directory -Path $outputDir -Force | Out-Null }
if (-not (Test-Path $outputInstallerDir)) { New-Item -ItemType Directory -Path $outputInstallerDir -Force | Out-Null }

function Get-MavenCmd($dir) {
    $wrapper = Join-Path $dir 'mvnw.cmd'
    if (Test-Path $wrapper) { return $wrapper }
    return 'mvn'
}

if (-not $OnlyInno) {
    Step "Build main project"
    try {
        $mainMvn = Get-MavenCmd $mainDir
        Log "Using Maven command: $mainMvn"
        Set-Location $mainDir
        & $mainMvn clean package | Tee-Object -FilePath $logFile -Append
    } catch { Fail "Main project build failed: $_" }
    Set-Location $PSScriptRoot

    Step "Build validator project"
    try {
        $validatorMvn = Get-MavenCmd $validatorDir
        Log "Using Maven command: $validatorMvn"
        Set-Location $validatorDir
        & $validatorMvn clean package | Tee-Object -FilePath $logFile -Append
    } catch { Fail "Validator project build failed: $_" }
    Set-Location $PSScriptRoot

    Step "Copy validator JAR to resources/output"
    try {
        $jar = Get-ChildItem "$validatorDir\target" -Filter "gomis-account-validator*-jar-with-dependencies.jar" | Select-Object -First 1
        if (-not $jar) { Fail "Validator JAR not found in $validatorDir\target" }
        $dest = Join-Path $outputDir "ValidateGomisUser.jar"
        Copy-Item $jar.FullName $dest -Force
        Log "Copied $($jar.Name) to $dest"
    } catch { Fail "Copy validator JAR failed: $_" }

    Step "Create GOMIS.exe with Launch4j"
    try {
        if (-not $launch4jExe) { Fail "launch4jc.exe not found in resources/launch4j or launch4j. Please ensure Launch4j is present." }
        if (-not $launch4jConfig) { Fail "launch4j-config.xml not found in resources/ or gomis-installer/. Please ensure the config is present." }
        & $launch4jExe $launch4jConfig | Tee-Object -FilePath $logFile -Append
        $exeName = "GOMIS.exe"
        $exePath = Join-Path $outputDir $exeName
        if (Test-Path $exePath) {
            Log "GOMIS.exe created at $exePath"
        } else {
            Fail "Expected $exeName not found in $outputDir after Launch4j run."
        }
    } catch { Fail "Launch4j step failed: $_" }
}

if (-not $SkipInno) {
    Step "Build installer with Inno Setup"
    try {
        if (-not (Test-Path $innoExe)) { Fail "ISCC.exe not found at $innoExe" }
        if (-not (Test-Path $innoScript)) { Fail "Inno Setup script not found at $innoScript" }
        # Run ISCC with working directory set to $PSScriptRoot so OutputDir is always gomis-installer/Output
        Push-Location $PSScriptRoot
        & $innoExe $innoScript | Tee-Object -FilePath $logFile -Append
        Pop-Location
        Log "Inno Setup build complete."
        # Check for Inno Setup errors in the log
        $logContent = Get-Content $logFile -Raw
        if ($logContent -match 'Error on line' -or $logContent -match 'Compile aborted.') {
            Write-Host "`nBUILD FAILED. See $logFile for details." -ForegroundColor Red
            Log "BUILD FAILED. See $logFile for details."
            exit 1
        }
        # List contents of Output directory
        $installerFiles = Get-ChildItem $outputInstallerDir | Select-Object Name, Length, LastWriteTime
        Log "Output directory contents after Inno Setup build:"
        foreach ($file in $installerFiles) {
            Log "  $($file.Name) ($($file.Length) bytes, $($file.LastWriteTime))"
        }
        Write-Host "`nOutput directory contents after Inno Setup build:" -ForegroundColor Yellow
        $installerFiles | Format-Table Name, Length, LastWriteTime
    } catch { Fail "Inno Setup build failed: $_" }
}

Step "BUILD COMPLETE. See $logFile for details."
Write-Host "`nBUILD COMPLETE. See $logFile for details." -ForegroundColor Green 