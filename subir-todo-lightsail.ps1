# Script para subir Backend y Frontend a AWS Lightsail
# Ejecutar desde PowerShell

$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"
$serverUser = "ubuntu"
$remotePath = "/home/ubuntu"

Write-Host "📤 Subiendo Backend y Frontend a AWS Lightsail..." -ForegroundColor Cyan
Write-Host ""

# Verificar clave
if (-not (Test-Path $keyPath)) {
    Write-Host "❌ No se encontró la clave SSH: $keyPath" -ForegroundColor Red
    exit 1
}

# ===== SUBIR BACKEND =====
Write-Host "📦 Subiendo Backend..." -ForegroundColor Yellow
$backendPath = "C:\Users\ByArc\Desktop\backend-vetclinic-1"

if (-not (Test-Path $backendPath)) {
    Write-Host "❌ No se encontró el backend: $backendPath" -ForegroundColor Red
    exit 1
}

Set-Location $backendPath

# Crear archivo temporal con exclusiones
$excludeFile = "$env:TEMP\scp-exclude.txt"
@(
    "node_modules",
    "target",
    "dist",
    ".git",
    "logs",
    "*.tar",
    "frontend-vetclinio-1"
) | Out-File -FilePath $excludeFile -Encoding utf8

# Subir backend usando tar para excluir archivos (más compatible)
Write-Host "   Comprimiendo y subiendo backend..." -ForegroundColor Gray

# Crear tar excluyendo archivos (si tienes tar en Windows)
# Si no tienes tar, subir todo y limpiar después
scp -i $keyPath -r `
    -o "StrictHostKeyChecking=no" `
    . $serverUser@${serverIP}:${remotePath}/backend-vetclinic-1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Backend subido exitosamente" -ForegroundColor Green
} else {
    Write-Host "❌ Error al subir backend" -ForegroundColor Red
    exit 1
}

# Limpiar archivo temporal
Remove-Item $excludeFile -ErrorAction SilentlyContinue

Write-Host ""

# ===== SUBIR FRONTEND =====
Write-Host "📦 Subiendo Frontend..." -ForegroundColor Yellow
$frontendPath = "C:\Users\ByArc\Desktop\frontend-vetclinio-1"

if (-not (Test-Path $frontendPath)) {
    Write-Host "❌ No se encontró el frontend: $frontendPath" -ForegroundColor Red
    Write-Host "   Verifica que el frontend esté en el escritorio" -ForegroundColor Yellow
    exit 1
}

Set-Location $frontendPath

# Subir frontend
Write-Host "   Subiendo frontend..." -ForegroundColor Gray
scp -i $keyPath -r `
    -o "StrictHostKeyChecking=no" `
    . $serverUser@${serverIP}:${remotePath}/frontend-vetclinio-1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Frontend subido exitosamente" -ForegroundColor Green
} else {
    Write-Host "❌ Error al subir frontend" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 ¡Código fuente subido exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos pasos en el servidor:" -ForegroundColor Cyan
Write-Host "   1. Conecta por SSH: ssh -i $keyPath ${serverUser}@${serverIP}"
Write-Host "   2. cd backend-vetclinic-1"
Write-Host "   3. Limpiar archivos innecesarios: rm -rf node_modules target dist .git logs *.tar"
Write-Host "   4. cp env.example .env"
Write-Host "   5. nano .env  (configura tus variables con IP: 3.232.108.231)"
Write-Host "   6. docker-compose -f docker-compose.lightsail.yml build"
Write-Host "   7. docker-compose -f docker-compose.lightsail.yml up -d"
Write-Host ""
