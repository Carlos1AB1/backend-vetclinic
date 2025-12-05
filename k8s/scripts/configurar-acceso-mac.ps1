# Script para configurar acceso IAM para usuario desde Mac
# Este script crea un usuario IAM y le da permisos para EKS y ECR
# Uso: .\configurar-acceso-mac.ps1 -Username [nombre-usuario] -AwsRegion [region]

param(
    [Parameter(Mandatory=$true)]
    [string]$Username,
    
    [Parameter(Mandatory=$false)]
    [string]$AwsRegion = "us-east-1",
    
    [Parameter(Mandatory=$false)]
    [string]$ClusterName = "vetclinic-eks"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Configuracion de Acceso IAM para Mac" -ForegroundColor Cyan
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

# Verificar si el usuario ya existe
Write-Host "[*] Verificando si el usuario '$Username' ya existe..." -ForegroundColor Yellow
$ErrorActionPreference = "SilentlyContinue"
$userCheck = aws iam get-user --user-name $Username 2>&1 | Out-Null
$userExists = $LASTEXITCODE -eq 0
$ErrorActionPreference = "Stop"

if ($userExists) {
    Write-Host "[!] El usuario '$Username' ya existe. Se actualizaran los permisos." -ForegroundColor Yellow
} else {
    Write-Host "[*] Creando usuario IAM: $Username..." -ForegroundColor Yellow
    $ErrorActionPreference = "Continue"
    $createResult = aws iam create-user --user-name $Username 2>&1
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] No se pudo crear el usuario" -ForegroundColor Red
        Write-Host $createResult -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Usuario creado exitosamente" -ForegroundColor Green
}
Write-Host ""

# Crear política para EKS
Write-Host "[*] Creando politica IAM para EKS..." -ForegroundColor Yellow
$eksPolicyName = "VetClinicEKSUserPolicy"
$eksPolicyArn = "arn:aws:iam::$accountId`:policy/$eksPolicyName"

# Crear archivo temporal con la política
$eksPolicyFile = New-TemporaryFile
$eksPolicyJson = '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Action":["eks:DescribeCluster","eks:ListClusters","eks:AccessKubernetesApi","eks:ListUpdates","eks:ListFargateProfiles","eks:ListNodegroups","eks:DescribeNodegroup","eks:DescribeUpdate","eks:DescribeFargateProfile"],"Resource":"arn:aws:eks:' + $AwsRegion + ':' + $accountId + ':cluster/' + $ClusterName + '"},{"Effect":"Allow","Action":["eks:ListClusters"],"Resource":"*"}]}'
[System.IO.File]::WriteAllText($eksPolicyFile.FullName, $eksPolicyJson)
$eksPolicyFilePath = "file://" + $eksPolicyFile.FullName.Replace('\', '/')

# Verificar si la política ya existe
$ErrorActionPreference = "SilentlyContinue"
$policyCheck = aws iam get-policy --policy-arn $eksPolicyArn 2>&1 | Out-Null
$policyExists = $LASTEXITCODE -eq 0
$ErrorActionPreference = "Stop"

if (-not $policyExists) {
    Write-Host "[*] Creando politica: $eksPolicyName..." -ForegroundColor Yellow
    $ErrorActionPreference = "Continue"
    $createPolicy = aws iam create-policy --policy-name $eksPolicyName --policy-document $eksPolicyFilePath --description "Politica para acceso a EKS cluster VetClinic" 2>&1
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] No se pudo crear la politica EKS" -ForegroundColor Red
        Write-Host $createPolicy -ForegroundColor Red
        Remove-Item $eksPolicyFile.FullName -ErrorAction SilentlyContinue
        exit 1
    }
    Write-Host "[OK] Politica EKS creada" -ForegroundColor Green
} else {
    Write-Host "[*] Actualizando politica existente: $eksPolicyName..." -ForegroundColor Yellow
    $ErrorActionPreference = "Continue"
    $policyVersion = aws iam create-policy-version --policy-arn $eksPolicyArn --policy-document $eksPolicyFilePath --set-as-default 2>&1
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Politica EKS actualizada" -ForegroundColor Green
    }
}
Remove-Item $eksPolicyFile.FullName -ErrorAction SilentlyContinue
Write-Host ""

# Crear política para ECR
Write-Host "[*] Creando politica IAM para ECR..." -ForegroundColor Yellow
$ecrPolicyName = "VetClinicECRUserPolicy"
$ecrPolicyArn = "arn:aws:iam::$accountId`:policy/$ecrPolicyName"

# Crear archivo temporal con la política ECR
$ecrPolicyFile = New-TemporaryFile
$ecrPolicyJson = '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Action":["ecr:GetAuthorizationToken","ecr:BatchCheckLayerAvailability","ecr:GetDownloadUrlForLayer","ecr:BatchGetImage","ecr:PutImage","ecr:InitiateLayerUpload","ecr:UploadLayerPart","ecr:CompleteLayerUpload","ecr:DescribeRepositories","ecr:ListImages","ecr:DescribeImages","ecr:TagResource","ecr:UntagResource"],"Resource":["arn:aws:ecr:' + $AwsRegion + ':' + $accountId + ':repository/vetclinic-backend","arn:aws:ecr:' + $AwsRegion + ':' + $accountId + ':repository/vetclinic-frontend"]},{"Effect":"Allow","Action":"ecr:GetAuthorizationToken","Resource":"*"}]}'
[System.IO.File]::WriteAllText($ecrPolicyFile.FullName, $ecrPolicyJson)
$ecrPolicyFilePath = "file://" + $ecrPolicyFile.FullName.Replace('\', '/')

# Verificar si la política ECR ya existe
$ErrorActionPreference = "SilentlyContinue"
$ecrPolicyCheck = aws iam get-policy --policy-arn $ecrPolicyArn 2>&1 | Out-Null
$ecrPolicyExists = $LASTEXITCODE -eq 0
$ErrorActionPreference = "Stop"

if (-not $ecrPolicyExists) {
    Write-Host "[*] Creando politica: $ecrPolicyName..." -ForegroundColor Yellow
    $ErrorActionPreference = "Continue"
    $createEcrPolicy = aws iam create-policy --policy-name $ecrPolicyName --policy-document $ecrPolicyFilePath --description "Politica para acceso a ECR repositories VetClinic" 2>&1
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] No se pudo crear la politica ECR" -ForegroundColor Red
        Write-Host $createEcrPolicy -ForegroundColor Red
        Remove-Item $ecrPolicyFile.FullName -ErrorAction SilentlyContinue
        exit 1
    }
    Write-Host "[OK] Politica ECR creada" -ForegroundColor Green
} else {
    Write-Host "[*] Actualizando politica existente: $ecrPolicyName..." -ForegroundColor Yellow
    $ErrorActionPreference = "Continue"
    $ecrPolicyVersion = aws iam create-policy-version --policy-arn $ecrPolicyArn --policy-document $ecrPolicyFilePath --set-as-default 2>&1
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Politica ECR actualizada" -ForegroundColor Green
    }
}
Remove-Item $ecrPolicyFile.FullName -ErrorAction SilentlyContinue
Write-Host ""

# Adjuntar políticas al usuario
Write-Host "[*] Adjuntando politicas al usuario..." -ForegroundColor Yellow

# Adjuntar política EKS
Write-Host "  - Adjuntando politica EKS..." -ForegroundColor Cyan
aws iam attach-user-policy --user-name $Username --policy-arn $eksPolicyArn 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "    [OK] Politica EKS adjuntada" -ForegroundColor Green
} else {
    Write-Host "    [!] La politica EKS ya estaba adjuntada o hubo un error" -ForegroundColor Yellow
}

# Adjuntar política ECR
Write-Host "  - Adjuntando politica ECR..." -ForegroundColor Cyan
aws iam attach-user-policy --user-name $Username --policy-arn $ecrPolicyArn 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "    [OK] Politica ECR adjuntada" -ForegroundColor Green
} else {
    Write-Host "    [!] La politica ECR ya estaba adjuntada o hubo un error" -ForegroundColor Yellow
}

# Adjuntar política AWS managed para kubectl (si es necesario)
Write-Host "  - Adjuntando politica AmazonEKSClusterAdminPolicy..." -ForegroundColor Cyan
$ErrorActionPreference = "Continue"
$adminPolicyResult = aws iam attach-user-policy --user-name $Username --policy-arn "arn:aws:iam::aws:policy/AmazonEKSClusterAdminPolicy" 2>&1
$ErrorActionPreference = "Stop"
if ($LASTEXITCODE -eq 0) {
    Write-Host "    [OK] Politica AmazonEKSClusterAdminPolicy adjuntada" -ForegroundColor Green
} else {
    Write-Host "    [!] La politica ya estaba adjuntada o no es necesaria" -ForegroundColor Yellow
}
Write-Host ""

# Crear access keys
Write-Host "[*] Creando Access Keys para el usuario..." -ForegroundColor Yellow
Write-Host "  [!] Si ya existen keys, se mostraran las existentes" -ForegroundColor Yellow

# Listar keys existentes
$existingKeys = aws iam list-access-keys --user-name $Username --query 'AccessKeyMetadata[?Status==`Active`]' --output json 2>&1 | ConvertFrom-Json

if ($existingKeys.Count -eq 0) {
    Write-Host "  [*] No hay keys activas, creando nuevas..." -ForegroundColor Cyan
    $newKeys = aws iam create-access-key --user-name $Username --output json 2>&1 | ConvertFrom-Json
    if ($LASTEXITCODE -eq 0) {
        $accessKeyId = $newKeys.AccessKey.AccessKeyId
        $secretAccessKey = $newKeys.AccessKey.SecretAccessKey
        Write-Host "  [OK] Access Keys creadas" -ForegroundColor Green
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  CREDENCIALES DE ACCESO" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Access Key ID:     $accessKeyId" -ForegroundColor White
        Write-Host "Secret Access Key: $secretAccessKey" -ForegroundColor White
        Write-Host ""
        Write-Host "[!] IMPORTANTE: Guarda estas credenciales de forma segura!" -ForegroundColor Red
        Write-Host "[!] El Secret Access Key solo se muestra UNA VEZ" -ForegroundColor Red
        Write-Host ""
    } else {
        Write-Host "  [ERROR] No se pudieron crear las Access Keys" -ForegroundColor Red
    }
} else {
    Write-Host "  [!] El usuario ya tiene Access Keys activas" -ForegroundColor Yellow
    Write-Host "  [!] Si necesitas nuevas keys, elimina las existentes primero" -ForegroundColor Yellow
    foreach ($key in $existingKeys) {
        Write-Host "    - Key ID: $($key.AccessKeyId) (Creada: $($key.CreateDate))" -ForegroundColor Cyan
    }
}
Write-Host ""

# Actualizar aws-auth ConfigMap en EKS para dar acceso al cluster
Write-Host "[*] Configurando acceso al cluster EKS..." -ForegroundColor Yellow
Write-Host "  [!] Necesitas ejecutar esto manualmente en el cluster:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  kubectl edit configmap aws-auth -n kube-system" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Agrega este bloque en la seccion mapUsers:" -ForegroundColor Cyan
Write-Host ""
Write-Host "  mapUsers: |" -ForegroundColor White
Write-Host "    - userarn: arn:aws:iam::$accountId`:user/$Username" -ForegroundColor White
Write-Host "      username: $Username" -ForegroundColor White
Write-Host "      groups:" -ForegroundColor White
Write-Host "        - system:masters" -ForegroundColor White
Write-Host ""

# Resumen
Write-Host "========================================" -ForegroundColor Green
Write-Host "  RESUMEN" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Usuario IAM:        $Username" -ForegroundColor White
Write-Host "Account ID:         $accountId" -ForegroundColor White
Write-Host "Region:             $AwsRegion" -ForegroundColor White
Write-Host "Cluster EKS:        $ClusterName" -ForegroundColor White
Write-Host ""
Write-Host "Politicas adjuntadas:" -ForegroundColor Cyan
Write-Host "  - $eksPolicyName" -ForegroundColor White
Write-Host "  - $ecrPolicyName" -ForegroundColor White
Write-Host "  - AmazonEKSClusterAdminPolicy" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  PROXIMOS PASOS EN MAC" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "1. Instala AWS CLI en Mac:" -ForegroundColor Cyan
Write-Host "   brew install awscli" -ForegroundColor White
Write-Host ""
Write-Host "2. Configura las credenciales:" -ForegroundColor Cyan
Write-Host "   aws configure --profile vetclinic" -ForegroundColor White
Write-Host "   (Usa las Access Keys creadas arriba)" -ForegroundColor White
Write-Host ""
Write-Host "3. Configura kubectl:" -ForegroundColor Cyan
Write-Host "   aws eks update-kubeconfig --region $AwsRegion --name $ClusterName --profile vetclinic" -ForegroundColor White
Write-Host ""
Write-Host "4. Verifica acceso:" -ForegroundColor Cyan
Write-Host "   kubectl get nodes --profile vetclinic" -ForegroundColor White
Write-Host ""
Write-Host "5. Configura acceso en el cluster (IMPORTANTE):" -ForegroundColor Cyan
Write-Host "   kubectl edit configmap aws-auth -n kube-system" -ForegroundColor White
Write-Host "   (Agrega el usuario como se mostro arriba)" -ForegroundColor White
Write-Host ""

Write-Host "[OK] Configuracion completada!" -ForegroundColor Green

