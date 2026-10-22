param(
    [switch]$BuildOnly
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$outputDir = Join-Path $projectRoot "build\classes"
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
}

$sourceFiles = @(Get-ChildItem -Path "$projectRoot\src\main\java" -Recurse -Filter "*.java" | ForEach-Object FullName)

if ($sourceFiles.Count -eq 0) {
    Write-Error "No java source files found in src/main/java"
    exit 1
}

Write-Host "Compiling $($sourceFiles.Count) Java files..." -ForegroundColor Yellow
& javac -encoding UTF-8 -d $outputDir $sourceFiles
if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed!"
    exit $LASTEXITCODE
}
Write-Host "Build succeeded!" -ForegroundColor Green

if ($BuildOnly) {
    exit 0
}

Write-Host "Launching Sanbaka Game..." -ForegroundColor Cyan
$cp = "$outputDir;$(Join-Path $projectRoot 'src\main\resources');$(Join-Path $projectRoot 'assets');$projectRoot"
$javaArgs = @(
    '-Dsun.java2d.d3d=false',
    '-Dsun.java2d.opengl=True',
    '-Dsanbaka.perfLog=false',
    '-cp', $cp,
    'sanbaka.SanbakaGame'
)

& java @javaArgs
exit $LASTEXITCODE

