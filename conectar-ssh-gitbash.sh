#!/bin/bash
# Script para conectar por SSH a AWS Lightsail desde Git Bash

KEY_PATH="$HOME/Downloads/VetClinic.pem"
SERVER_IP="3.232.108.231"
USER="ubuntu"

# Verificar que existe la clave
if [ ! -f "$KEY_PATH" ]; then
    echo "❌ No se encontró la clave SSH en: $KEY_PATH"
    echo "   Verifica que el archivo VetClinic.pem esté en Descargas"
    exit 1
fi

# Dar permisos a la clave (requerido en Linux/Git Bash)
chmod 400 "$KEY_PATH"

echo "🔐 Conectando a $USER@$SERVER_IP..."
echo ""

# Conectar por SSH
ssh -i "$KEY_PATH" $USER@$SERVER_IP

