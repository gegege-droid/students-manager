@echo off
setlocal
cd /d "%~dp0backend"
set "JAVA_HOME=C:\Users\lenovo\.jdks\corretto-17.0.19"

if not exist "ruoyi-admin\target\ruoyi-admin.jar" (
  echo [ERROR] ruoyi-admin.jar not found.
  echo Run build-backend.bat first.
  pause
  exit /b 1
)

echo Starting RuoYi backend on http://localhost:8080 ...
"%JAVA_HOME%\bin\java.exe" -jar "ruoyi-admin\target\ruoyi-admin.jar"
pause
