/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

# GOMIS Project Root Build Script
# Calls the main build script in gomis-installer, passing all arguments

param(
    [Parameter(ValueFromRemainingArguments=$true)]
    $Args
)

$installerScript = Join-Path $PSScriptRoot 'gomis-installer\build-all.ps1'
if (-not (Test-Path $installerScript)) {
    Write-Host "ERROR: Could not find gomis-installer/build-all.ps1" -ForegroundColor Red
    exit 1
}

Write-Host "Running build-all.ps1 from project root..." -ForegroundColor Cyan
& $installerScript @Args 