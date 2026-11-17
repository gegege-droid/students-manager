@echo off
setlocal
cd /d "%~dp0backend"

echo Building backend with JDK 17 ...
call mvn -DskipTests clean package
set "BUILD_STATUS=%ERRORLEVEL%"
if not "%BUILD_STATUS%"=="0" (
  echo [ERROR] Backend build failed.
  pause
  exit /b %BUILD_STATUS%
)

echo Build finished.
pause
