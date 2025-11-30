# Script para subir código fuente a AWS Lightsail
# Ejecutar desde PowerShell en tu máquina local

$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"
$serverUser = "ubuntu"
$remotePath = "/home/ubuntu"

Write-Host "📤 Subiendo código fuente a AWS Lightsail..." -ForegroundColor Cyan
Write-Host ""

# Verificar que existe la clave
if (-not (Test-Path $keyPath)) {
    Write-Host "❌ No se encontró la clave SSH: $keyPath" -ForegroundColor Red
    exit 1
}

# Ir al directorio del backend
$backendPath = "C:\Users\ByArc\Desktop\backend-vetclinic-1"
if (-not (Test-Path $backendPath)) {
    Write-Host "❌ No se encontró el directorio del backend: $backendPath" -ForegroundColor Red
    exit 1
}

Set-Location $backendPath

Write-Host "📦 Subiendo backend..." -ForegroundColor Yellow
# Subir backend completo (excluyendo node_modules, target, etc.)
scp -i $keyPath -r `
    --exclude="node_modules" `
    --exclude="target" `
    --exclude="dist" `
    --exclude=".git" `
    --exclude="logs" `
    --exclude="*.tar" `
    . $serverUser@${serverIP}:${remotePath}/backend-vetclinic-1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Backend subido exitosamente" -ForegroundColor Green
} else {
    Write-Host "❌ Error al subir backend" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📦 Subiendo frontend..." -ForegroundColor Yellow

# Verificar si frontend está dentro del backend
$frontendPath = Join-Path $backendPath "frontend-vetclinio-1"
if (Test-Path $frontendPath) {
    # Frontend está dentro del backend
    Write-Host "✅ Frontend encontrado dentro del backend" -ForegroundColor Green
    Write-Host "   (Ya se subió junto con el backend)" -ForegroundColor Gray
} else {
    # Frontend está fuera, buscar en el escritorio
    $frontendPath = "C:\Users\ByArc\Desktop\frontend-vetclinio-1"
    if (Test-Path $frontendPath) {
        Set-Location $frontendPath
        scp -i $keyPath -r `
            --exclude="node_modules" `
            --exclude="dist" `
            --exclude=".git" `
            . $serverUser@${serverIP}:${remotePath}/frontend-vetclinio-1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Frontend subido exitosamente" -ForegroundColor Green
        } else {
            Write-Host "❌ Error al subir frontend" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️  No se encontró el frontend. Asegúrate de subirlo manualmente." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🎉 ¡Código fuente subido exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos pasos en el servidor:" -ForegroundColor Cyan
Write-Host "   1. Conecta por SSH: ssh -i $keyPath $serverUser@${serverIP}"
Write-Host "   2. cd backend-vetclinic-1"
Write-Host "   3. cp env.example .env"
Write-Host "   4. nano .env  (configura tus variables)"
Write-Host "   5. docker-compose -f docker-compose.lightsail.yml build"
Write-Host "   6. docker-compose -f docker-compose.lightsail.yml up -d"
Write-Host ""

