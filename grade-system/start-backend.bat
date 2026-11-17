@echo off
setlocal
cd /d "%~dp0backend"

if not exist "ruoyi-admin\target\ruoyi-admin.jar" (
  echo [ERROR] ruoyi-admin.jar not found.
  echo Run build-backend.bat first.
  pause
  exit /b 1
)

echo Starting RuoYi backend on http://localhost:8080 ...
set "JAVA_EXE=java"
if defined JAVA_HOME set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
"%JAVA_EXE%" -jar "ruoyi-admin\target\ruoyi-admin.jar"
pause
