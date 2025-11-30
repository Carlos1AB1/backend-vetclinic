# Script PowerShell para crear el secret de Kubernetes
# Uso: .\create-secret.ps1

$ErrorActionPreference = "Stop"

Write-Host "[*] Creando secret de Kubernetes..." -ForegroundColor Cyan

# Leer valores
$dbPassword = Read-Host "Ingrese la contraseña de la base de datos" -AsSecureString
$jwtSecret = Read-Host "Ingrese el JWT Secret (o presione Enter para generar uno aleatorio)"
$mailUsername = Read-Host "Ingrese el usuario de email (Gmail)"
$mailPassword = Read-Host "Ingrese la contraseña de email (App Password)" -AsSecureString

# Generar JWT Secret si no se proporciono
if ([string]::IsNullOrWhiteSpace($jwtSecret)) {
    $jwtSecret = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
    Write-Host "JWT Secret generado: $jwtSecret" -ForegroundColor Yellow
}

# Convertir SecureStrings a texto plano
$dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
)
$mailPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($mailPassword)
)

# Crear archivo temporal con el secret
$secretYaml = @"
apiVersion: v1
kind: Secret
metadata:
  name: backend-secrets
  namespace: vetclinic
type: Opaque
stringData:
  DATABASE_PASSWORD: "$dbPasswordPlain"
  JWT_SECRET: "$jwtSecret"
  MAIL_USERNAME: "$mailUsername"
  MAIL_PASSWORD: "$mailPasswordPlain"
"@

# Preguntar por Twilio (opcional)
$useTwilio = Read-Host "Desea configurar Twilio? (s/n)"
if ($useTwilio -eq "s" -or $useTwilio -eq "S") {
    $twilioSid = Read-Host "Ingrese Twilio Account SID"
    $twilioToken = Read-Host "Ingrese Twilio Auth Token" -AsSecureString
    $twilioTokenPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($twilioToken)
    )
    $twilioPhone = Read-Host "Ingrese Twilio Phone Number"
    
    $secretYaml += @"

  TWILIO_ACCOUNT_SID: "$twilioSid"
  TWILIO_AUTH_TOKEN: "$twilioTokenPlain"
  TWILIO_PHONE_NUMBER: "$twilioPhone"
"@
}

# Guardar en archivo
$secretPath = Join-Path (Split-Path -Parent $PWD) "secret-backend.yaml"
$secretYaml | Out-File -FilePath $secretPath -Encoding utf8

Write-Host "[OK] Secret creado en $secretPath" -ForegroundColor Green
Write-Host "[!] Asegurate de aplicar este secret con: kubectl apply -f $secretPath" -ForegroundColor Yellow
