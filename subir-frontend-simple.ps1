# Script simple para subir Frontend
$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"

Write-Host "Subiendo Frontend..." -ForegroundColor Yellow

cd C:\Users\ByArc\Desktop\frontend-vetclinio-1

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: No se encontró el frontend en C:\Users\ByArc\Desktop\frontend-vetclinio-1" -ForegroundColor Red
    exit 1
}

scp -i $keyPath -r -o "StrictHostKeyChecking=no" . ubuntu@${serverIP}:/home/ubuntu/frontend-vetclinio-1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Frontend subido exitosamente" -ForegroundColor Green
} else {
    Write-Host "Error al subir frontend" -ForegroundColor Red
}

