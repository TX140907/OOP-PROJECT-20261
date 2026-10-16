@echo off
setlocal
chcp 65001 >nul
pushd "%~dp0"
if errorlevel 1 exit /b 1
set "MAVEN_CMD=mvn.cmd"
where mvn.cmd >nul 2>nul
if not errorlevel 1 goto mavenReady
set "MAVEN_CMD=%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd"
if exist "%MAVEN_CMD%" goto mavenReady
echo Maven 3.9+ is required. Install Maven and add its bin directory to PATH.
popd
exit /b 1
:mavenReady
call "%MAVEN_CMD%" -q "-Dmaven.repo.local=%~dp0.tools/m2" -DskipTests package
if errorlevel 1 goto failed
if /i "%~1"=="--build-only" goto success
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar "target\exam-run.jar" %*
if errorlevel 1 goto failed
:success
popd
exit /b 0
:failed
popd
exit /b 1
