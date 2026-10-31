# 启动后端（Spring Boot，端口 8080）
# 前置条件：MySQL 8 已启动，且已执行 sql/grade_manage.sql 与 sql/demo_data.sql
# 用法：右键“使用 PowerShell 运行”，或在终端执行 .\start-backend.ps1
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $root 'backend\target\grade-server-1.0.0.jar'

if (-not (Test-Path $jar)) {
    Write-Host '未找到 jar，正在用 Maven 打包（首次会下载依赖，较慢）...' -ForegroundColor Cyan
    Push-Location (Join-Path $root 'backend')
    mvn -B -DskipTests package
    Pop-Location
}

Write-Host ''
Write-Host '后端启动中：http://localhost:8080' -ForegroundColor Green
Write-Host '接口文档：http://localhost:8080/swagger-ui.html' -ForegroundColor Green
Write-Host '演示账号：学生 20230101 / 123456，班主任 T001 / 123456' -ForegroundColor Yellow
Write-Host ''
java "-Dfile.encoding=UTF-8" -jar $jar
