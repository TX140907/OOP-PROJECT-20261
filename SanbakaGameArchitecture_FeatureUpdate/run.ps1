$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'src\main\java\sanbaka'
$outputDir = Join-Path $projectRoot 'out'
$javaHome = 'C:\Program Files\Java\jdk-27\bin'
$javacExe = Join-Path $javaHome 'javac.exe'
$javaExe = Join-Path $javaHome 'java.exe'

if (-not (Test-Path -LiteralPath $javacExe) -or -not (Test-Path -LiteralPath $javaExe)) {
    Write-Error "Không tìm thấy JDK 27 tại $javaHome"
    exit 1
}

Set-Location -LiteralPath $projectRoot
$sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -Filter '*.java' | ForEach-Object FullName)
if ($sourceFiles.Count -eq 0) {
    Write-Error "Không tìm thấy file Java trong $sourceRoot"
    exit 1
}

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
& $javacExe -encoding UTF-8 -d $outputDir $sourceFiles
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$javaArgs = @('-Dsun.java2d.d3d=false', '-Dsun.java2d.opengl=True', '-Dsanbaka.perfLog=true', '-cp', $outputDir, 'sanbaka.SanbakaGame')
& $javaExe @javaArgs
exit $LASTEXITCODE

