#!/bin/bash

# Script para construir y subir imágenes Docker a AWS ECR
# Uso: ./build-and-push.sh [backend|frontend|all] [aws-region] [aws-account-id] [image-tag]

set -e

COMPONENT="${1:-all}"
AWS_REGION="${2:-us-east-1}"
AWS_ACCOUNT_ID="${3:-340914758022}"
IMAGE_TAG="${4:-latest}"

ECR_REPOSITORY_BACKEND="vetclinic-backend"
ECR_REPOSITORY_FRONTEND="vetclinic-frontend"
ECR_BASE_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Login a ECR
echo "[*] Autenticando con AWS ECR..." 
aws ecr get-login-password --region "$AWS_REGION" | \
    docker login --username AWS --password-stdin "$ECR_BASE_URL"

if [ $? -eq 0 ]; then
    echo "[OK] Autenticación exitosa"
else
    echo "[ERROR] Error al autenticar con ECR"
    exit 1
fi

# Función para construir y subir backend
build_and_push_backend() {
    # Determinar ruta del backend (desde k8s/scripts/)
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    K8S_DIR="$(dirname "$SCRIPT_DIR")"
    BACKEND_DIR="$(dirname "$K8S_DIR")"
    DOCKERFILE_PATH="$BACKEND_DIR/Dockerfile"
    
    if [ ! -f "$DOCKERFILE_PATH" ]; then
        echo "[ERROR] No se encontró el Dockerfile del backend en: $DOCKERFILE_PATH"
        exit 1
    fi
    
    echo "[*] Construyendo imagen del backend desde: $BACKEND_DIR" 
    
    # Detectar si estamos en macOS con Apple Silicon y necesitamos especificar plataforma
    ARCH=$(uname -m)
    if [[ "$ARCH" == "arm64" ]] || [[ "$ARCH" == "aarch64" ]]; then
        echo "   Detectado: macOS con Apple Silicon (ARM64)"
        echo "   Construyendo para plataforma: linux/amd64 (compatibilidad con EKS)"
        docker build --platform linux/amd64 -t "${ECR_REPOSITORY_BACKEND}:${IMAGE_TAG}" \
            -f "$DOCKERFILE_PATH" "$BACKEND_DIR"
    else
        echo "   Construyendo para plataforma nativa"
        docker build -t "${ECR_REPOSITORY_BACKEND}:${IMAGE_TAG}" \
            -f "$DOCKERFILE_PATH" "$BACKEND_DIR"
    fi
    
    if [ $? -ne 0 ]; then
        echo "[ERROR] Error al construir imagen del backend"
        exit 1
    fi
    
    echo "[*] Etiquetando imagen..." 
    docker tag "${ECR_REPOSITORY_BACKEND}:${IMAGE_TAG}" \
        "${ECR_BASE_URL}/${ECR_REPOSITORY_BACKEND}:${IMAGE_TAG}"
    
    echo "[*] Subiendo imagen del backend a ECR..." 
    docker push "${ECR_BASE_URL}/${ECR_REPOSITORY_BACKEND}:${IMAGE_TAG}"
    
    if [ $? -ne 0 ]; then
        echo "[ERROR] Error al subir imagen del backend"
        exit 1
    fi
    
    echo "[OK] Backend subido exitosamente" 
}

# Función para construir y subir frontend
build_and_push_frontend() {
    API_BASE_URL="${VITE_API_BASE_URL:-https://api.vetclinicpro.app/api}"
    
    # Determinar ruta del frontend (desde k8s/scripts/)
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    K8S_DIR="$(dirname "$SCRIPT_DIR")"
    BACKEND_DIR="$(dirname "$K8S_DIR")"
    FRONTEND_DIR="$BACKEND_DIR/frontend-vetclinio-1"
    DOCKERFILE_PATH="$FRONTEND_DIR/Dockerfile"
    
    if [ ! -f "$DOCKERFILE_PATH" ]; then
        echo "[ERROR] No se encontró el Dockerfile del frontend en: $DOCKERFILE_PATH"
        echo "   Verifica que el frontend esté en: $FRONTEND_DIR"
        exit 1
    fi
    
    echo "[*] Construyendo imagen del frontend desde: $FRONTEND_DIR" 
    
    # Detectar si estamos en macOS con Apple Silicon y necesitamos especificar plataforma
    ARCH=$(uname -m)
    if [[ "$ARCH" == "arm64" ]] || [[ "$ARCH" == "aarch64" ]]; then
        echo "   Detectado: macOS con Apple Silicon (ARM64)"
        echo "   Construyendo para plataforma: linux/amd64 (compatibilidad con EKS)"
        docker build --platform linux/amd64 -t "${ECR_REPOSITORY_FRONTEND}:${IMAGE_TAG}" \
            --build-arg VITE_API_BASE_URL="$API_BASE_URL" \
            -f "$DOCKERFILE_PATH" "$FRONTEND_DIR"
    else
        echo "   Construyendo para plataforma nativa"
        docker build -t "${ECR_REPOSITORY_FRONTEND}:${IMAGE_TAG}" \
            --build-arg VITE_API_BASE_URL="$API_BASE_URL" \
            -f "$DOCKERFILE_PATH" "$FRONTEND_DIR"
    fi
    
    if [ $? -ne 0 ]; then
        echo "[ERROR] Error al construir imagen del frontend"
        exit 1
    fi
    
    echo "[*] Etiquetando imagen..." 
    docker tag "${ECR_REPOSITORY_FRONTEND}:${IMAGE_TAG}" \
        "${ECR_BASE_URL}/${ECR_REPOSITORY_FRONTEND}:${IMAGE_TAG}"
    
    echo "[*] Subiendo imagen del frontend a ECR..." 
    docker push "${ECR_BASE_URL}/${ECR_REPOSITORY_FRONTEND}:${IMAGE_TAG}"
    
    if [ $? -ne 0 ]; then
        echo "[ERROR] Error al subir imagen del frontend"
        exit 1
    fi
    
    echo "[OK] Frontend subido exitosamente" 
}

# Ejecutar según el componente
case "$COMPONENT" in
    "backend")
        build_and_push_backend
        ;;
    "frontend")
        build_and_push_frontend
        ;;
    "all")
        build_and_push_backend
        build_and_push_frontend
        ;;
    *)
        echo "Uso: $0 [backend|frontend|all] [aws-region] [aws-account-id] [image-tag]"
        echo ""
        echo "Ejemplos:"
        echo "  $0 backend"
        echo "  $0 backend us-east-1 340914758022 latest"
        echo "  $0 all"
        exit 1
        ;;
esac

echo "[OK] Proceso completado!" 

