# Script para agregar usuario IAM al ConfigMap aws-auth de EKS
# Uso: .\agregar-usuario-eks.ps1 -Username [nombre-usuario] -ClusterName [nombre-cluster]

param(
    [Parameter(Mandatory=$true)]
    [string]$Username,
    
    [Parameter(Mandatory=$false)]
    [string]$ClusterName = "vetclinic-eks",
    
    [Parameter(Mandatory=$false)]
    [string]$Namespace = "kube-system",
    
    [Parameter(Mandatory=$false)]
    [string]$AwsRegion = "us-east-1"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Agregar Usuario IAM a EKS Cluster" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Obtener Account ID
Write-Host "[*] Obteniendo AWS Account ID..." -ForegroundColor Yellow
$accountId = aws sts get-caller-identity --query Account --output text 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] No se pudo obtener el Account ID. Verifica tus credenciales de AWS." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Account ID: $accountId" -ForegroundColor Green
Write-Host ""

# Verificar que el usuario existe
Write-Host "[*] Verificando que el usuario '$Username' existe..." -ForegroundColor Yellow
$userExists = aws iam get-user --user-name $Username 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] El usuario '$Username' no existe en IAM." -ForegroundColor Red
    Write-Host "   Crea el usuario primero con: .\configurar-acceso-mac.ps1 -Username $Username" -ForegroundColor Yellow
    exit 1
}
Write-Host "[OK] Usuario verificado" -ForegroundColor Green
Write-Host ""

# Verificar acceso al cluster
Write-Host "[*] Verificando acceso al cluster '$ClusterName'..." -ForegroundColor Yellow
$clusterExists = aws eks describe-cluster --name $ClusterName --region $AwsRegion 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] No se pudo acceder al cluster o no existe." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Cluster accesible" -ForegroundColor Green
Write-Host ""

# Obtener ConfigMap actual
Write-Host "[*] Obteniendo ConfigMap aws-auth..." -ForegroundColor Yellow
$configMap = kubectl get configmap aws-auth -n $Namespace -o yaml 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] No se pudo obtener el ConfigMap. Verifica acceso a kubectl." -ForegroundColor Red
    exit 1
}

# Verificar si el usuario ya está en el ConfigMap
if ($configMap -match "userarn.*$Username") {
    Write-Host "[!] El usuario '$Username' ya está en el ConfigMap aws-auth" -ForegroundColor Yellow
    Write-Host "   ¿Deseas actualizarlo? (S/N): " -ForegroundColor Yellow -NoNewline
    $response = Read-Host
    if ($response -ne "S" -and $response -ne "s") {
        Write-Host "[*] Operacion cancelada" -ForegroundColor Yellow
        exit 0
    }
}

# Crear archivo temporal con el ConfigMap
$tempFile = [System.IO.Path]::GetTempFileName()
$configMap | Out-File -FilePath $tempFile -Encoding UTF8

Write-Host "[*] Editando ConfigMap..." -ForegroundColor Yellow
Write-Host "   Archivo temporal: $tempFile" -ForegroundColor Cyan

# Leer el contenido
$content = Get-Content $tempFile -Raw

# Preparar el bloque del usuario
$userBlock = "  - userarn: arn:aws:iam::$accountId`:user/$Username`n    username: $Username`n    groups:`n      - system:masters`n"

# Verificar si existe mapUsers
if ($content -match "mapUsers:\s*\|\s*") {
    # Ya existe mapUsers, agregar el usuario
    if ($content -notmatch "userarn.*$Username") {
        # Agregar después de mapUsers: |
        $content = $content -replace "(mapUsers:\s*\|\s*)", "`$1`n$userBlock"
        Write-Host "[OK] Usuario agregado a mapUsers existente" -ForegroundColor Green
    } else {
        Write-Host "[!] El usuario ya existe en mapUsers" -ForegroundColor Yellow
    }
} else {
    # No existe mapUsers, crear la sección
    if ($content -match "data:") {
        $content = $content -replace "(data:)", "`$1`n  mapUsers: |`n$userBlock"
        Write-Host "[OK] Seccion mapUsers creada y usuario agregado" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] No se pudo encontrar la seccion 'data:' en el ConfigMap" -ForegroundColor Red
        Remove-Item $tempFile
        exit 1
    }
}

# Guardar el contenido modificado
$content | Out-File -FilePath $tempFile -Encoding UTF8 -NoNewline

# Aplicar el ConfigMap
Write-Host "[*] Aplicando ConfigMap actualizado..." -ForegroundColor Yellow
kubectl apply -f $tempFile 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] ConfigMap actualizado exitosamente" -ForegroundColor Green
} else {
    Write-Host "[ERROR] No se pudo aplicar el ConfigMap" -ForegroundColor Red
    Write-Host "   Puedes editarlo manualmente con: kubectl edit configmap aws-auth -n $Namespace" -ForegroundColor Yellow
    Remove-Item $tempFile
    exit 1
}

# Limpiar archivo temporal
Remove-Item $tempFile

# Verificar
Write-Host "[*] Verificando ConfigMap actualizado..." -ForegroundColor Yellow
$updatedConfigMap = kubectl get configmap aws-auth -n $Namespace -o yaml 2>&1
if ($updatedConfigMap -match "userarn.*$Username") {
    Write-Host "[OK] Usuario '$Username' confirmado en ConfigMap" -ForegroundColor Green
} else {
    Write-Host "[!] Advertencia: No se pudo confirmar el usuario en el ConfigMap" -ForegroundColor Yellow
    Write-Host "   Verifica manualmente con: kubectl get configmap aws-auth -n $Namespace -o yaml" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  CONFIGURACION COMPLETADA" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Usuario: $Username" -ForegroundColor White
Write-Host "Cluster: $ClusterName" -ForegroundColor White
Write-Host "Account ID: $accountId" -ForegroundColor White
Write-Host ""
Write-Host "El usuario ahora puede acceder al cluster desde Mac con:" -ForegroundColor Cyan
Write-Host "  aws eks update-kubeconfig --region $AwsRegion --name $ClusterName --profile vetclinic" -ForegroundColor White
Write-Host ""

