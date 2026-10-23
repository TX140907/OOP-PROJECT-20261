@echo off
setlocal EnableDelayedExpansion
pushd "%~dp0"
if errorlevel 1 exit /b 1

if not exist "build\classes" mkdir "build\classes"
if errorlevel 1 goto failed

(for /r "src\main\java" %%f in (*.java) do (
    set "source=%%f"
    @echo "!source:\=/!"
)) > "build\sources.txt"

javac -encoding UTF-8 -d "build/classes" @"build/sources.txt"
if errorlevel 1 goto failed

if /i "%~1"=="--build-only" goto success

java -Dsun.awt.noerasebackground=true -Dsun.java2d.d3d=false -Dsun.java2d.transaccel=true -Dsun.java2d.ddforcevram=true -Dsun.java2d.noddraw=false -Dsun.java2d.opengl=true -Dsanbaka.perfLog=false -cp "build/classes;src/main/resources;assets;src/main/java;." sanbaka.SanbakaGame
if errorlevel 1 goto failed

:success
popd
exit /b 0

:failed
popd
exit /b 1

