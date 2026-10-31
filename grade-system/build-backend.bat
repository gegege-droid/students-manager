@echo off
setlocal
cd /d "%~dp0backend"
set "JAVA_HOME=C:\Users\lenovo\.jdks\corretto-17.0.19"

echo Building backend with JDK 17 ...
"D:\IDEA\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" ^
  -s "D:\IDEA\03maven\apache-maven-3.5.3\conf\settings.xml" ^
  -DskipTests clean package

echo.
echo Build finished. Check whether ruoyi-admin\target\ruoyi-admin.jar exists.
pause
