# Script para subir Frontend excluyendo archivos grandes
$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"
$frontendPath = "C:\Users\ByArc\Desktop\frontend-vetclinio-1"

Write-Host "Subiendo Frontend (sin node_modules ni dist)..." -ForegroundColor Yellow

cd $frontendPath

# Crear lista temporal de archivos a excluir
$excludeItems = @("node_modules", "dist", ".git")

# Usar robocopy para copiar excluyendo carpetas (más rápido y eficiente)
$tempDir = "$env:TEMP\frontend-temp-$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

Write-Host "Copiando archivos necesarios..." -ForegroundColor Gray
robocopy $frontendPath $tempDir /E /XD node_modules dist .git /NFL /NDL /NJH /NJS

# Subir desde el directorio temporal
Write-Host "Subiendo al servidor..." -ForegroundColor Gray
scp -i $keyPath -r -o "StrictHostKeyChecking=no" $tempDir\* ubuntu@${serverIP}:/home/ubuntu/frontend-vetclinio-1

# Limpiar directorio temporal
Remove-Item -Recurse -Force $tempDir

if ($LASTEXITCODE -eq 0) {
    Write-Host "Frontend subido exitosamente" -ForegroundColor Green
} else {
    Write-Host "Error al subir frontend" -ForegroundColor Red
}

