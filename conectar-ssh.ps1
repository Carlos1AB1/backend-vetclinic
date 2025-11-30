# Script para conectar por SSH a AWS Lightsail
# IP del servidor: 3.232.108.231

$keyPath = "$env:USERPROFILE\Downloads\VetClinic.pem"
$serverIP = "3.232.108.231"
$user = "ubuntu"

# Verificar que existe la clave
if (Test-Path $keyPath) {
    Write-Host "✅ Clave SSH encontrada: $keyPath" -ForegroundColor Green
    Write-Host "🔐 Conectando a $user@$serverIP..." -ForegroundColor Yellow
    Write-Host ""
    
    # Conectar por SSH
    ssh -i $keyPath $user@$serverIP
} else {
    Write-Host "❌ No se encontró la clave SSH en: $keyPath" -ForegroundColor Red
    Write-Host "Por favor, descarga la clave desde Lightsail Console" -ForegroundColor Yellow
}

