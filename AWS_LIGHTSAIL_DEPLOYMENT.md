# 🚀 Guía Completa de Despliegue en AWS Lightsail

Esta guía te ayudará a desplegar VetClinic Pro completamente dockerizado en AWS Lightsail.

## 📋 Prerrequisitos

- Cuenta de AWS (puedes crear una en aws.amazon.com)
- Tarjeta de crédito (solo para verificación, no se cobra en el free tier)
- Acceso SSH configurado
- Tu aplicación dockerizada lista

## 💰 Costos

- **Free Tier**: 3 meses gratis ($10/mes de crédito)
- **Después**: ~$3.50-10/mes (dependiendo del plan)
- **Base de datos**: Incluida en la instancia o RDS separado (~$15/mes)

## 🎯 Paso 1: Crear Instancia en AWS Lightsail

### 1.1 Acceder a Lightsail

1. Ve a [AWS Lightsail Console](https://lightsail.aws.amazon.com/)
2. Inicia sesión con tu cuenta de AWS
3. Haz clic en **"Create instance"**

### 1.2 Configurar la Instancia

**Plataforma:**
- Selecciona **Linux/Unix**

**Blueprint:**
- Selecciona **"OS Only"**
- Elige **Ubuntu 22.04 LTS** (o la versión más reciente)

**Instance plan:**
- Para empezar: **$3.50/mes** (512 MB RAM, 1 vCPU) - Suficiente para desarrollo
- Para producción: **$5-10/mes** (1-2 GB RAM, 1-2 vCPU) - Recomendado

**Nombre de la instancia:**
- Ejemplo: `vetclinic-production`

**Zona de disponibilidad:**
- Elige la más cercana a tus usuarios

### 1.3 Crear la Instancia

1. Haz clic en **"Create instance"**
2. Espera 1-2 minutos a que se cree

## 🔐 Paso 2: Configurar Acceso SSH

### 2.1 Obtener Clave SSH

1. En la consola de Lightsail, ve a tu instancia
2. Haz clic en la pestaña **"Account"**
3. Descarga la clave SSH (archivo `.pem`)

### 2.2 Conectar por SSH

**En Windows (PowerShell):**
```powershell
# Navegar a donde está tu clave .pem
cd C:\Users\ByArc\Downloads

# Conectar (reemplaza con tu IP y nombre de archivo)
ssh -i vetclinic-production-key.pem ubuntu@<tu-ip-publica>
```

**En Linux/Mac:**
```bash
# Dar permisos a la clave
chmod 400 vetclinic-production-key.pem

# Conectar
ssh -i vetclinic-production-key.pem ubuntu@<tu-ip-publica>
```

**Nota:** La IP pública la encuentras en la consola de Lightsail, en la pestaña "Networking"

## 🐳 Paso 3: Instalar Docker y Docker Compose

Una vez conectado por SSH, ejecuta:

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker
sudo apt install -y docker.io

# Iniciar Docker
sudo systemctl start docker
sudo systemctl enable docker

# Agregar usuario ubuntu al grupo docker
sudo usermod -aG docker ubuntu

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker --version
docker-compose --version

# Cerrar sesión y volver a conectar para aplicar cambios de grupo
exit
```

**Vuelve a conectar por SSH** para que los cambios del grupo docker tomen efecto.

## 📦 Paso 4: Subir tu Aplicación

Tienes 3 opciones:

### Opción A: Clonar desde Git (Recomendado)

```bash
# Instalar Git
sudo apt install -y git

# Clonar tu repositorio
git clone <tu-repositorio-backend> backend-vetclinic-1
cd backend-vetclinic-1

# Si tienes el frontend en otro repo
cd ..
git clone <tu-repositorio-frontend> frontend-vetclinio-1
```

### Opción B: Subir archivo .tar

```bash
# En tu máquina local (PowerShell)
scp -i vetclinic-production-key.pem vetclinic-images.tar ubuntu@<tu-ip>:/home/ubuntu/

# En el servidor
cd ~
docker load -i vetclinic-images.tar
```

### Opción C: Subir archivos con SCP

```bash
# Desde tu máquina local
scp -i vetclinic-production-key.pem -r backend-vetclinic-1 ubuntu@<tu-ip>:/home/ubuntu/
```

## ⚙️ Paso 5: Configurar Variables de Entorno

```bash
# Crear archivo .env
cd ~/backend-vetclinic-1
nano .env
```

Agrega todas las variables necesarias:

```env
# Base de datos
POSTGRES_PASSWORD=tu-password-segura-aqui
DATABASE_URL=jdbc:postgresql://postgres:5432/vetclinic_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu-password-segura-aqui

# JWT
JWT_SECRET=tu-jwt-secret-super-seguro-genera-con-openssl-rand-base64-32

# Email
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password-gmail

# Twilio
TWILIO_ACCOUNT_SID=tu-account-sid
TWILIO_AUTH_TOKEN=tu-auth-token
TWILIO_PHONE_NUMBER=+1234567890

# URLs (cambiar por tu dominio o IP)
FRONTEND_URL=http://<tu-ip-publica>:5173
BACKEND_URL=http://<tu-ip-publica>:8081
CORS_ORIGINS=http://<tu-ip-publica>:5173,http://<tu-ip-publica>

# Aplicación
CLINIC_NAME=VetClinic Pro
```

Guardar: `Ctrl+O`, Enter, `Ctrl+X`

## 🚀 Paso 6: Ejecutar la Aplicación

### Opción A: Con docker-compose (Recomendado)

```bash
cd ~/backend-vetclinic-1

# Construir imágenes (si no usaste .tar)
docker-compose build

# O si ya cargaste el .tar, modifica docker-compose.yml para usar image: en lugar de build:

# Ejecutar en producción
docker-compose -f docker-compose.prod.yml up -d

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f

# Ver estado
docker-compose -f docker-compose.prod.yml ps
```

### Opción B: Ejecutar manualmente

```bash
# Crear red
docker network create vetclinic-network

# PostgreSQL
docker run -d \
  --name postgres \
  --network vetclinic-network \
  -e POSTGRES_DB=vetclinic_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=tu-password \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:16-alpine

# Esperar 15 segundos
sleep 15

# Backend
docker run -d \
  --name vetclinic-backend \
  --network vetclinic-network \
  -p 8081:8081 \
  --env-file .env \
  backend-vetclinic-1-backend:latest

# Frontend
docker run -d \
  --name vetclinic-frontend \
  -p 80:80 \
  backend-vetclinic-1-frontend:latest
```

## 🔥 Paso 7: Configurar Firewall (Puertos)

1. En Lightsail Console, ve a tu instancia
2. Haz clic en la pestaña **"Networking"**
3. Haz clic en **"Add rule"** y agrega:

   - **Application**: Custom
   - **Protocol**: TCP
   - **Port**: 80 (HTTP)
   - **Source**: Anywhere (0.0.0.0/0)

   Repite para:
   - Puerto 443 (HTTPS)
   - Puerto 8081 (Backend API)
   - Puerto 5173 (Frontend, si no usas 80)

## ✅ Paso 8: Verificar que Funciona

```bash
# Ver contenedores corriendo
docker ps

# Ver logs del backend
docker logs vetclinic-backend -f

# Ver logs del frontend
docker logs vetclinic-frontend -f

# Probar desde el servidor
curl http://localhost:8081/api/actuator/health
curl http://localhost
```

**Acceder desde tu navegador:**
- Frontend: `http://<tu-ip-publica>`
- Backend: `http://<tu-ip-publica>:8081/api`
- Health Check: `http://<tu-ip-publica>:8081/api/actuator/health`

## 🌐 Paso 9: Configurar Dominio (Opcional)

### 9.1 Obtener un Dominio

- Comprar en Route 53, Namecheap, GoDaddy, etc.

### 9.2 Configurar DNS

1. En tu proveedor de dominio, crea registros A:
   - `@` → `<tu-ip-publica>` (para dominio principal)
   - `www` → `<tu-ip-publica>` (para www)

### 9.3 Actualizar Variables de Entorno

```bash
nano .env
```

Cambia:
```env
FRONTEND_URL=https://tu-dominio.com
BACKEND_URL=https://api.tu-dominio.com
CORS_ORIGINS=https://tu-dominio.com,https://www.tu-dominio.com
```

Reinicia los contenedores:
```bash
docker-compose -f docker-compose.prod.yml restart
```

## 🔒 Paso 10: Configurar SSL/HTTPS (Opcional pero Recomendado)

### Opción A: Usar Nginx como Reverse Proxy

```bash
# Instalar Nginx
sudo apt install -y nginx

# Instalar Certbot
sudo apt install -y certbot python3-certbot-nginx

# Configurar Nginx (ver configuración abajo)
sudo nano /etc/nginx/sites-available/vetclinic

# Habilitar sitio
sudo ln -s /etc/nginx/sites-available/vetclinic /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# Obtener certificado SSL
sudo certbot --nginx -d tu-dominio.com -d www.tu-dominio.com
```

**Configuración de Nginx** (`/etc/nginx/sites-available/vetclinic`):

```nginx
server {
    listen 80;
    server_name tu-dominio.com www.tu-dominio.com;

    # Frontend
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 📊 Paso 11: Configurar Monitoreo y Backups

### 11.1 Script de Backup Automático

```bash
# Crear script de backup
nano ~/backup.sh
```

Contenido:
```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/home/ubuntu/backups"

mkdir -p $BACKUP_DIR

# Backup de base de datos
docker exec postgres pg_dump -U postgres vetclinic_db > $BACKUP_DIR/db_backup_$DATE.sql

# Backup de volúmenes
docker run --rm -v postgres_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/volumes_backup_$DATE.tar.gz /data

# Eliminar backups antiguos (mantener últimos 7 días)
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete
```

Hacer ejecutable:
```bash
chmod +x ~/backup.sh
```

Agregar a cron (backup diario a las 2 AM):
```bash
crontab -e
```

Agregar línea:
```
0 2 * * * /home/ubuntu/backup.sh
```

## 🔄 Paso 12: Actualizar la Aplicación

### Cuando hagas cambios:

```bash
# Conectar por SSH
ssh -i clave.pem ubuntu@<ip>

# Ir al directorio
cd ~/backend-vetclinic-1

# Actualizar código
git pull

# Reconstruir y reiniciar
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f
```

## 🛠️ Comandos Útiles

```bash
# Ver estado de contenedores
docker ps

# Ver logs
docker-compose -f docker-compose.prod.yml logs -f

# Reiniciar servicios
docker-compose -f docker-compose.prod.yml restart

# Detener todo
docker-compose -f docker-compose.prod.yml down

# Ver uso de recursos
docker stats

# Limpiar espacio
docker system prune -a
```

## 🚨 Solución de Problemas

### El backend no se conecta a PostgreSQL

```bash
# Verificar que PostgreSQL está corriendo
docker ps | grep postgres

# Ver logs de PostgreSQL
docker logs postgres

# Verificar red Docker
docker network ls
docker network inspect vetclinic-network
```

### Puerto ya en uso

```bash
# Ver qué está usando el puerto
sudo netstat -tulpn | grep :8081

# Detener contenedor que usa el puerto
docker stop <container-id>
```

### No puedo acceder desde el navegador

1. Verificar firewall en Lightsail (puertos abiertos)
2. Verificar que contenedores están corriendo: `docker ps`
3. Verificar logs: `docker logs vetclinic-frontend`

### Problemas de memoria

Si la instancia se queda sin memoria:

```bash
# Ver uso de memoria
free -h

# Limpiar Docker
docker system prune -a

# Considerar upgrade a plan con más RAM
```

## 📈 Escalar (Futuro)

Cuando necesites más recursos:

1. En Lightsail Console → Tu instancia
2. **"Change instance plan"**
3. Selecciona plan con más RAM/CPU
4. La instancia se reiniciará automáticamente

## 💡 Tips Finales

1. **Siempre haz backups** antes de actualizar
2. **Monitorea los logs** regularmente
3. **Configura alertas** en Lightsail para uso de recursos
4. **Usa variables de entorno** para configuración sensible
5. **Mantén Docker actualizado**: `sudo apt update && sudo apt upgrade docker.io`

## ✅ Checklist Final

- [ ] Instancia Lightsail creada
- [ ] Docker y Docker Compose instalados
- [ ] Aplicación subida al servidor
- [ ] Variables de entorno configuradas
- [ ] Contenedores ejecutándose
- [ ] Puertos abiertos en firewall
- [ ] Aplicación accesible desde navegador
- [ ] Backups configurados
- [ ] SSL configurado (opcional)
- [ ] Dominio configurado (opcional)

## 🎉 ¡Listo!

Tu aplicación debería estar funcionando en AWS Lightsail. 

**URLs de acceso:**
- Frontend: `http://<tu-ip-publica>`
- Backend API: `http://<tu-ip-publica>:8081/api`

¡Felicitaciones! 🚀

