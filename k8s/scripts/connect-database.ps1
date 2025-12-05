# Script para conectar y visualizar la base de datos PostgreSQL en Kubernetes
# Uso: .\connect-database.ps1 [port-forward|psql|info]

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("port-forward", "psql", "info", "all")]
    [string]$Action = "info",
    
    [Parameter(Mandatory=$false)]
    [int]$LocalPort = 5432
)

$ErrorActionPreference = "Stop"
$Namespace = "vetclinic"
$ServiceName = "postgres-service"
$DatabaseName = "vetclinic_db"
$DatabaseUser = "postgres"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Conexion a PostgreSQL en Kubernetes" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Funcion para obtener informacion del pod
function Get-PodInfo {
    Write-Host "[*] Obteniendo informacion del pod de PostgreSQL..." -ForegroundColor Yellow
    
    $pod = kubectl get pods -n $Namespace -l app=postgres -o jsonpath='{.items[0].metadata.name}' 2>&1
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($pod)) {
    Write-Host "[ERROR] No se encontro el pod de PostgreSQL en el namespace '$Namespace'" -ForegroundColor Red
    Write-Host "   Verifica que el pod este corriendo con: kubectl get pods -n $Namespace" -ForegroundColor Yellow
        exit 1
    }
    
    $podStatus = kubectl get pod $pod -n $Namespace -o jsonpath='{.status.phase}' 2>&1
    Write-Host "[OK] Pod encontrado: $pod (Estado: $podStatus)" -ForegroundColor Green
    Write-Host ""
    
    return $pod
}

# Funcion para obtener la contrasena del secret
function Get-DatabasePassword {
    Write-Host "[*] Obteniendo contrasena de la base de datos..." -ForegroundColor Yellow
    
    $password = kubectl get secret backend-secrets -n $Namespace -o jsonpath='{.data.DATABASE_PASSWORD}' 2>&1
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($password)) {
        Write-Host "[ERROR] No se pudo obtener la contrasena del secret 'backend-secrets'" -ForegroundColor Red
        Write-Host "   Verifica que el secret exista con: kubectl get secrets -n $Namespace" -ForegroundColor Yellow
        exit 1
    }
    
    # Decodificar base64
    $decodedPassword = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($password))
    Write-Host "[OK] Contrasena obtenida" -ForegroundColor Green
    Write-Host ""
    
    return $decodedPassword
}

# Funcion para mostrar informacion
function Show-Info {
    $pod = Get-PodInfo
    $password = Get-DatabasePassword
    
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  INFORMACION DE CONEXION" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Namespace:        $Namespace" -ForegroundColor White
    Write-Host "Pod:              $pod" -ForegroundColor White
    Write-Host "Service:          $ServiceName" -ForegroundColor White
    Write-Host "Database:         $DatabaseName" -ForegroundColor White
    Write-Host "User:             $DatabaseUser" -ForegroundColor White
    Write-Host "Password:         [OCULTA]" -ForegroundColor White
    Write-Host "Port:             5432" -ForegroundColor White
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  OPCIONES DE CONEXION" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "1. Port Forward (para herramientas gráficas):" -ForegroundColor Cyan
    Write-Host "   .\connect-database.ps1 -Action port-forward" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "2. Conexión directa con psql:" -ForegroundColor Cyan
    Write-Host "   .\connect-database.ps1 -Action psql" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "3. Comandos manuales:" -ForegroundColor Cyan
    Write-Host "   kubectl port-forward -n $Namespace svc/$ServiceName $LocalPort`:5432" -ForegroundColor Yellow
    Write-Host "   kubectl exec -it -n $Namespace $pod -- psql -U $DatabaseUser -d $DatabaseName" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  CONEXION CON HERRAMIENTAS GRAFICAS" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Despues de ejecutar port-forward, usa estos datos:" -ForegroundColor Cyan
    Write-Host "  Host:     localhost" -ForegroundColor White
    Write-Host "  Port:     $LocalPort" -ForegroundColor White
    Write-Host "  Database: $DatabaseName" -ForegroundColor White
    Write-Host "  User:     $DatabaseUser" -ForegroundColor White
    Write-Host "  Password: [La contrasena del secret]" -ForegroundColor White
    Write-Host ""
}

# Funcion para hacer port-forward
function Start-PortForward {
    $pod = Get-PodInfo
    
    Write-Host "[*] Iniciando port-forward..." -ForegroundColor Yellow
    Write-Host "   Puerto local: $LocalPort -> Puerto remoto: 5432" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Presiona Ctrl+C para detener el port-forward" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Una vez iniciado, puedes conectarte con:" -ForegroundColor Green
    Write-Host "  - Host: localhost" -ForegroundColor White
    Write-Host "  - Port: $LocalPort" -ForegroundColor White
    Write-Host "  - Database: $DatabaseName" -ForegroundColor White
    Write-Host "  - User: $DatabaseUser" -ForegroundColor White
    Write-Host ""
    
    kubectl port-forward -n $Namespace svc/$ServiceName $LocalPort`:5432
}

# Funcion para conectar con psql
function Connect-Psql {
    $pod = Get-PodInfo
    
    Write-Host "[*] Conectando a PostgreSQL con psql..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Escribe 'exit' para salir de psql" -ForegroundColor Cyan
    Write-Host ""
    
    kubectl exec -it -n $Namespace $pod -- psql -U $DatabaseUser -d $DatabaseName
}

# Ejecutar según la acción
switch ($Action) {
    "info" {
        Show-Info
    }
    "port-forward" {
        Start-PortForward
    }
    "psql" {
        Connect-Psql
    }
    "all" {
        Show-Info
        Write-Host ""
        Write-Host "Deseas iniciar port-forward ahora? (S/N): " -ForegroundColor Yellow -NoNewline
        $response = Read-Host
        if ($response -eq "S" -or $response -eq "s" -or $response -eq "Y" -or $response -eq "y") {
            Start-PortForward
        }
    }
}

