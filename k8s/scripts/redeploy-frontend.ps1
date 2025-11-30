# Script para reconstruir y redesplegar el frontend con la URL correcta del backend
# Uso: .\redeploy-frontend.ps1

$ErrorActionPreference = "Stop"

# Configuracion
$AwsAccountId = "340914758022"
$AwsRegion = "us-east-1"
$BackendLoadBalancerUrl = "http://afc0efb3c665b415f89f9c27aa07dd2e-888876140.us-east-1.elb.amazonaws.com:8081/api"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "  REDEPLOY FRONTEND CON URL CORRECTA" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backend URL: $BackendLoadBalancerUrl" -ForegroundColor Yellow
Write-Host ""

# Paso 1: Configurar variable de entorno
Write-Host "[1/5] Configurando variable de entorno..." -ForegroundColor Green
$env:VITE_API_BASE_URL = $BackendLoadBalancerUrl

# Paso 2: Reconstruir y subir imagen
Write-Host "[2/5] Reconstruyendo y subiendo imagen del frontend..." -ForegroundColor Green
cd "$PSScriptRoot"
& .\build-and-push.ps1 -Component frontend -AwsAccountId $AwsAccountId -AwsRegion $AwsRegion

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Error al construir/subir imagen" -ForegroundColor Red
    exit 1
}

# Paso 3: Aplicar cambios en deployment (opcional, ya que usamos imagePullPolicy: Always)
Write-Host "[3/5] Aplicando configuracion de Kubernetes..." -ForegroundColor Green
cd ..
kubectl apply -f deployment-frontend.yaml

# Paso 4: Forzar restart de pods
Write-Host "[4/5] Reiniciando pods del frontend..." -ForegroundColor Green
kubectl rollout restart deployment/frontend -n vetclinic
kubectl rollout status deployment/frontend -n vetclinic

# Paso 5: Obtener URL del frontend
Write-Host "[5/5] Obteniendo URL del frontend..." -ForegroundColor Green
$frontendUrl = kubectl get service frontend-lb -n vetclinic -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

Write-Host ""
Write-Host "====================================" -ForegroundColor Green
Write-Host "  DESPLIEGUE COMPLETADO!" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host ""
Write-Host "Tu frontend esta disponible en:" -ForegroundColor Cyan
Write-Host "  http://$frontendUrl" -ForegroundColor Yellow
Write-Host ""
Write-Host "Esta URL funcionara desde CUALQUIER dispositivo con internet" -ForegroundColor Green
Write-Host ""
