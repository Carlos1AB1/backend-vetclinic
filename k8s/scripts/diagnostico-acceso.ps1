# Script de Diagnostico de Acceso Publico a Kubernetes
# Uso: .\diagnostico-acceso.ps1

param(
    [string]$Namespace = "vetclinic"
)

$ErrorActionPreference = "Continue"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  DIAGNOSTICO DE ACCESO PUBLICO - K8S/EKS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 1. Obtener servicios LoadBalancer
Write-Host "[1/5] Obteniendo servicios LoadBalancer..." -ForegroundColor Yellow
$services = kubectl get svc -n $Namespace -o json | ConvertFrom-Json
$lbServices = $services.items | Where-Object { $_.spec.type -eq "LoadBalancer" }

if ($lbServices.Count -eq 0) {
    Write-Host "  X No se encontraron servicios tipo LoadBalancer" -ForegroundColor Red
    exit 1
}

Write-Host "  OK Servicios LoadBalancer encontrados: $($lbServices.Count)" -ForegroundColor Green
foreach ($svc in $lbServices) {
    $name = $svc.metadata.name
    $hostname = $svc.status.loadBalancer.ingress[0].hostname
    $port = $svc.spec.ports[0].port
    $nodePort = $svc.spec.ports[0].nodePort
    Write-Host "    - $name : $hostname`:$port (NodePort: $nodePort)" -ForegroundColor Cyan
}

# 2. Verificar estado de pods
Write-Host "`n[2/5] Verificando estado de pods..." -ForegroundColor Yellow
$pods = kubectl get pods -n $Namespace -o json | ConvertFrom-Json
$runningPods = ($pods.items | Where-Object { $_.status.phase -eq "Running" }).Count
$totalPods = $pods.items.Count
Write-Host "  OK Pods Running: $runningPods/$totalPods" -ForegroundColor Green

# 3. Probar conectividad HTTP
Write-Host "`n[3/5] Probando conectividad HTTP..." -ForegroundColor Yellow
foreach ($svc in $lbServices) {
    $name = $svc.metadata.name
    $hostname = $svc.status.loadBalancer.ingress[0].hostname
    $port = $svc.spec.ports[0].port
    
    try {
        $url = "http://$hostname`:$port"
        if ($name -like "*backend*") {
            $url += "/api/actuator/health"
        }
        
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 10 -UseBasicParsing
        Write-Host "  OK $name responde correctamente (Status: $($response.StatusCode))" -ForegroundColor Green
    }
    catch {
        Write-Host "  X $name NO responde: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4. Verificar Health Checks
Write-Host "`n[4/5] Verificando Health Checks..." -ForegroundColor Yellow
foreach ($svc in $lbServices) {
    $hostname = $svc.status.loadBalancer.ingress[0].hostname
    $lbName = $hostname.Split('.')[0]
    
    try {
        $health = aws elb describe-instance-health --load-balancer-name $lbName --region us-east-1 --output json 2>$null | ConvertFrom-Json
        $inService = ($health.InstanceStates | Where-Object { $_.State -eq "InService" }).Count
        $total = $health.InstanceStates.Count
        
        if ($inService -eq $total) {
            Write-Host "  OK $lbName : $inService/$total instancias saludables" -ForegroundColor Green
        }
        else {
            Write-Host "  ! $lbName : $inService/$total instancias saludables" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "  ! No se pudo verificar health check para $lbName" -ForegroundColor Yellow
    }
}

# 5. Resumen
Write-Host "`n[5/5] RESUMEN Y URLs DE ACCESO" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Cyan
foreach ($svc in $lbServices) {
    $hostname = $svc.status.loadBalancer.ingress[0].hostname
    $port = $svc.spec.ports[0].port
    $name = $svc.metadata.name
    
    Write-Host "`n$name :" -ForegroundColor Green
    Write-Host "  http://$hostname`:$port" -ForegroundColor Yellow
}
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "Estas URLs deben funcionar desde CUALQUIER dispositivo" -ForegroundColor Green
Write-Host "con conexion a internet (PC, movil, tablet, etc.)" -ForegroundColor Green
Write-Host ""
