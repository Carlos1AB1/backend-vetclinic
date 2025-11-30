# Script PowerShell para construir y subir imagenes Docker a AWS ECR
# Uso: .\build-and-push.ps1 -Component [backend|frontend|all] -AwsRegion [region] -AwsAccountId [account-id]

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("backend", "frontend", "all")]
    [string]$Component = "all",
    
    [Parameter(Mandatory=$false)]
    [string]$AwsRegion = "us-east-1",
    
    [Parameter(Mandatory=$true)]
    [string]$AwsAccountId,
    
    [Parameter(Mandatory=$false)]
    [string]$ImageTag = "latest"
)

$ErrorActionPreference = "Stop"

$ECR_REPOSITORY_BACKEND = "vetclinic-backend"
$ECR_REPOSITORY_FRONTEND = "vetclinic-frontend"
$ECR_BASE_URL = "$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com"

# Login a ECR
Write-Host "[*] Autenticando con AWS ECR..." -ForegroundColor Cyan
$password = aws ecr get-login-password --region $AwsRegion 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Error al obtener password de ECR" -ForegroundColor Red
    Write-Host $password -ForegroundColor Red
    exit 1
}
$password = $password.Trim()
echo $password | docker login --username AWS --password-stdin $ECR_BASE_URL
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Error al autenticar con ECR" -ForegroundColor Red
    Write-Host "Intentando metodo alternativo..." -ForegroundColor Yellow
    # Metodo alternativo
    $loginCmd = "echo $password | docker login --username AWS --password-stdin $ECR_BASE_URL"
    cmd /c $loginCmd
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Error al autenticar con ECR (metodo alternativo)" -ForegroundColor Red
        exit 1
    }
}
Write-Host "[OK] Autenticacion exitosa" -ForegroundColor Green

# Funcion para construir y subir backend
function Build-AndPush-Backend {
    # Determinar ruta del backend (desde k8s/scripts/)
    $scriptsDir = Get-Location
    $k8sDir = Split-Path -Parent $scriptsDir
    $backendDir = Split-Path -Parent $k8sDir
    $dockerfilePath = Join-Path $backendDir "Dockerfile"
    
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "[ERROR] No se encontro el Dockerfile del backend en: $dockerfilePath" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "[*] Construyendo imagen del backend desde: $backendDir" -ForegroundColor Yellow
    docker build -t "$ECR_REPOSITORY_BACKEND`:$ImageTag" -f $dockerfilePath $backendDir
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Error al construir imagen del backend" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "[*] Etiquetando imagen..." -ForegroundColor Yellow
    docker tag "$ECR_REPOSITORY_BACKEND`:$ImageTag" "$ECR_BASE_URL/$ECR_REPOSITORY_BACKEND`:$ImageTag"
    
    Write-Host "[*] Subiendo imagen del backend a ECR..." -ForegroundColor Yellow
    docker push "$ECR_BASE_URL/$ECR_REPOSITORY_BACKEND`:$ImageTag"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Error al subir imagen del backend" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "[OK] Backend subido exitosamente" -ForegroundColor Green
}

# Funcion para construir y subir frontend
function Build-AndPush-Frontend {
    $apiBaseUrl = if ($env:VITE_API_BASE_URL) { $env:VITE_API_BASE_URL } else { "https://api.vetclinic.com/api" }
    
    # Determinar ruta del frontend (desde k8s/scripts/)
    $scriptsDir = Get-Location
    $k8sDir = Split-Path -Parent $scriptsDir
    $backendDir = Split-Path -Parent $k8sDir
    $frontendDir = Join-Path $backendDir "frontend-vetclinio-1"
    $dockerfilePath = Join-Path $frontendDir "Dockerfile"
    
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "[ERROR] No se encontro el Dockerfile del frontend en: $dockerfilePath" -ForegroundColor Red
        Write-Host "   Verifica que el frontend este en: $frontendDir" -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "[*] Construyendo imagen del frontend desde: $frontendDir" -ForegroundColor Yellow
    docker build -t "$ECR_REPOSITORY_FRONTEND`:$ImageTag" `
        --build-arg VITE_API_BASE_URL=$apiBaseUrl `
        -f $dockerfilePath $frontendDir
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Error al construir imagen del frontend" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "[*] Etiquetando imagen..." -ForegroundColor Yellow
    docker tag "$ECR_REPOSITORY_FRONTEND`:$ImageTag" "$ECR_BASE_URL/$ECR_REPOSITORY_FRONTEND`:$ImageTag"
    
    Write-Host "[*] Subiendo imagen del frontend a ECR..." -ForegroundColor Yellow
    docker push "$ECR_BASE_URL/$ECR_REPOSITORY_FRONTEND`:$ImageTag"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Error al subir imagen del frontend" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "[OK] Frontend subido exitosamente" -ForegroundColor Green
}

# Ejecutar segun el componente
switch ($Component) {
    "backend" {
        Build-AndPush-Backend
    }
    "frontend" {
        Build-AndPush-Frontend
    }
    "all" {
        Build-AndPush-Backend
        Build-AndPush-Frontend
    }
}

Write-Host "[OK] Proceso completado!" -ForegroundColor Green
