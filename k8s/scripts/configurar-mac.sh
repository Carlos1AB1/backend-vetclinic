#!/bin/bash

# Script para configurar acceso AWS desde Mac
# Uso: ./configurar-mac.sh

set -e

AWS_REGION="us-east-1"
CLUSTER_NAME="vetclinic-eks"
AWS_ACCOUNT_ID="340914758022"
PROFILE="vetclinic"

echo "========================================"
echo "  Configuracion AWS desde Mac"
echo "========================================"
echo ""

# Verificar AWS CLI
if ! command -v aws &> /dev/null; then
    echo "[ERROR] AWS CLI no esta instalado"
    echo "  Instala con: brew install awscli"
    exit 1
fi
echo "[OK] AWS CLI instalado"

# Verificar kubectl
if ! command -v kubectl &> /dev/null; then
    echo "[!] kubectl no esta instalado"
    echo "  Instala con: brew install kubectl"
    read -p "  ¿Deseas continuar de todas formas? (s/n): " continue
    if [[ ! $continue =~ ^[Ss]$ ]]; then
        exit 1
    fi
else
    echo "[OK] kubectl instalado"
fi

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "[!] Docker no esta instalado"
    echo "  Instala Docker Desktop desde: https://www.docker.com/products/docker-desktop"
    read -p "  ¿Deseas continuar de todas formas? (s/n): " continue
    if [[ ! $continue =~ ^[Ss]$ ]]; then
        exit 1
    fi
else
    echo "[OK] Docker instalado"
fi

echo ""

# Verificar si el perfil ya existe
if aws configure list --profile $PROFILE &> /dev/null; then
    echo "[!] El perfil '$PROFILE' ya existe"
    read -p "  ¿Deseas reconfigurarlo? (s/n): " reconfigure
    if [[ $reconfigure =~ ^[Ss]$ ]]; then
        aws configure --profile $PROFILE
    else
        echo "[*] Usando perfil existente"
    fi
else
    echo "[*] Configurando perfil AWS..."
    echo "  Ingresa tus credenciales cuando se te solicite:"
    aws configure --profile $PROFILE
fi

echo ""

# Verificar credenciales
echo "[*] Verificando credenciales..."
IDENTITY=$(aws sts get-caller-identity --profile $PROFILE 2>&1)
if [ $? -eq 0 ]; then
    echo "[OK] Credenciales validas"
    echo "$IDENTITY" | grep -o '"Arn": "[^"]*"' | head -1
else
    echo "[ERROR] Las credenciales no son validas"
    exit 1
fi

echo ""

# Configurar kubectl
if command -v kubectl &> /dev/null; then
    echo "[*] Configurando acceso a EKS cluster..."
    aws eks update-kubeconfig \
        --region $AWS_REGION \
        --name $CLUSTER_NAME \
        --profile $PROFILE
    
    if [ $? -eq 0 ]; then
        echo "[OK] kubectl configurado"
        
        # Verificar acceso
        echo "[*] Verificando acceso al cluster..."
        if kubectl get nodes &> /dev/null; then
            echo "[OK] Acceso al cluster verificado"
            kubectl get nodes
        else
            echo "[!] No se pudo acceder al cluster"
            echo "  Verifica que tu usuario este en el ConfigMap aws-auth"
            echo "  Ejecuta desde Windows: .\\agregar-usuario-eks.ps1 -Username tu-usuario"
        fi
    else
        echo "[ERROR] No se pudo configurar kubectl"
    fi
fi

echo ""

# Configurar ECR
echo "[*] Configurando acceso a ECR..."
ECR_LOGIN=$(aws ecr get-login-password --region $AWS_REGION --profile $PROFILE 2>&1)
if [ $? -eq 0 ]; then
    echo "$ECR_LOGIN" | docker login --username AWS --password-stdin \
        $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
    
    if [ $? -eq 0 ]; then
        echo "[OK] Login a ECR exitoso"
    else
        echo "[!] No se pudo hacer login a ECR (Docker puede no estar corriendo)"
    fi
else
    echo "[ERROR] No se pudo obtener password de ECR"
fi

echo ""

# Resumen
echo "========================================"
echo "  CONFIGURACION COMPLETADA"
echo "========================================"
echo ""
echo "Perfil AWS:        $PROFILE"
echo "Region:            $AWS_REGION"
echo "Cluster EKS:       $CLUSTER_NAME"
echo "Account ID:        $AWS_ACCOUNT_ID"
echo ""
echo "Comandos utiles:"
echo "  kubectl get nodes"
echo "  kubectl get pods -n vetclinic"
echo "  aws ecr describe-repositories --profile $PROFILE"
echo ""

