#!/bin/bash
# Script de despliegue para AWS Lightsail
# Ejecutar en el servidor después de subir los archivos

set -e  # Salir si hay error

echo "🚀 Iniciando despliegue en AWS Lightsail..."

# Verificar que Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Instalando..."
    sudo apt update
    sudo apt install -y docker.io
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
    echo "✅ Docker instalado. Por favor, cierra sesión y vuelve a conectar."
    exit 1
fi

# Verificar que Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose no está instalado. Instalando..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose instalado"
fi

# Verificar que existe el archivo .env
if [ ! -f .env ]; then
    echo "⚠️  Archivo .env no encontrado. Creando desde env.example..."
    if [ -f env.example ]; then
        cp env.example .env
        echo "📝 Por favor, edita el archivo .env con tus valores:"
        echo "   nano .env"
        exit 1
    else
        echo "❌ No se encontró env.example. Crea un archivo .env manualmente."
        exit 1
    fi
fi

# Cargar imágenes si existe el .tar
if [ -f vetclinic-images.tar ]; then
    echo "📦 Cargando imágenes desde .tar..."
    docker load -i vetclinic-images.tar
    echo "✅ Imágenes cargadas"
else
    echo "🔨 Construyendo imágenes desde Dockerfiles..."
    docker-compose build
    echo "✅ Imágenes construidas"
fi

# Detener contenedores existentes
echo "🛑 Deteniendo contenedores existentes..."
docker-compose -f docker-compose.lightsail.yml down 2>/dev/null || true

# Iniciar servicios
echo "🚀 Iniciando servicios..."
docker-compose -f docker-compose.lightsail.yml up -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 10

# Verificar estado
echo "📊 Estado de los contenedores:"
docker-compose -f docker-compose.lightsail.yml ps

# Verificar health checks
echo "🏥 Verificando health checks..."
sleep 5

if curl -f http://localhost:8081/api/actuator/health > /dev/null 2>&1; then
    echo "✅ Backend está respondiendo"
else
    echo "⚠️  Backend no responde aún. Revisa los logs:"
    echo "   docker-compose -f docker-compose.lightsail.yml logs backend"
fi

if curl -f http://localhost > /dev/null 2>&1; then
    echo "✅ Frontend está respondiendo"
else
    echo "⚠️  Frontend no responde aún. Revisa los logs:"
    echo "   docker-compose -f docker-compose.lightsail.yml logs frontend"
fi

echo ""
echo "🎉 Despliegue completado!"
echo ""
echo "📝 URLs de acceso:"
echo "   Frontend: http://$(curl -s ifconfig.me)"
echo "   Backend:  http://$(curl -s ifconfig.me):8081/api"
echo ""
echo "📋 Comandos útiles:"
echo "   Ver logs:     docker-compose -f docker-compose.lightsail.yml logs -f"
echo "   Ver estado:   docker-compose -f docker-compose.lightsail.yml ps"
echo "   Reiniciar:    docker-compose -f docker-compose.lightsail.yml restart"
echo "   Detener:      docker-compose -f docker-compose.lightsail.yml down"
echo ""

