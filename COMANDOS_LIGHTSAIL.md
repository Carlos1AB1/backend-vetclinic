# 🚀 Comandos para Ejecutar en AWS Lightsail

## Paso 1: Actualizar el Sistema

```bash
sudo apt update && sudo apt upgrade -y
```

## Paso 2: Instalar Docker

```bash
# Instalar Docker
sudo apt install -y docker.io

# Iniciar Docker
sudo systemctl start docker
sudo systemctl enable docker

# Agregar usuario ubuntu al grupo docker
sudo usermod -aG docker ubuntu

# Verificar instalación
docker --version
```

## Paso 3: Instalar Docker Compose

```bash
# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker-compose --version
```

## Paso 4: Cerrar y Reconectar

**IMPORTANTE:** Cierra esta sesión y vuelve a conectar para que los cambios del grupo docker tomen efecto.

En Lightsail:
1. Cierra la terminal actual
2. Abre una nueva terminal desde Lightsail Console
3. O reconecta por SSH

## Paso 5: Verificar que Docker Funciona

```bash
# Debería funcionar sin sudo
docker ps

# Si pide sudo, cierra y reconecta de nuevo
```

## Paso 6: Subir tu Aplicación

### Opción A: Clonar desde Git (Recomendado)

```bash
# Instalar Git
sudo apt install -y git

# Clonar repositorio
git clone <tu-repositorio-backend> backend-vetclinic-1
cd backend-vetclinic-1
```

### Opción B: Subir archivo .tar desde tu máquina local

**En tu máquina Windows (PowerShell):**
```powershell
# Navegar a donde está tu .tar
cd C:\Users\ByArc\Desktop\backend-vetclinic-1

# Subir el .tar (reemplaza con tu IP y nombre de clave)
scp -i <tu-clave.pem> vetclinic-images.tar ubuntu@3.232.108.231:/home/ubuntu/
```

**En el servidor:**
```bash
# Cargar imágenes
docker load -i vetclinic-images.tar

# Verificar que se cargaron
docker images
```

## Paso 7: Configurar Variables de Entorno

```bash
# Crear archivo .env
nano .env
```

Pega esto y ajusta los valores:

```env
POSTGRES_PASSWORD=tu-password-segura-aqui
DATABASE_URL=jdbc:postgresql://postgres:5432/vetclinic_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu-password-segura-aqui
JWT_SECRET=genera-una-clave-secreta-muy-larga-y-segura
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password-gmail
TWILIO_ACCOUNT_SID=tu-account-sid
TWILIO_AUTH_TOKEN=tu-auth-token
TWILIO_PHONE_NUMBER=+1234567890
FRONTEND_URL=http://3.232.108.231
BACKEND_URL=http://3.232.108.231:8081
CORS_ORIGINS=http://3.232.108.231,http://3.232.108.231:5173
CLINIC_NAME=VetClinic Pro
```

Guardar: `Ctrl+O`, Enter, `Ctrl+X`

## Paso 8: Ejecutar la Aplicación

```bash
# Si subiste el .tar, usar docker-compose.lightsail.yml
docker-compose -f docker-compose.lightsail.yml up -d

# Si clonaste el repo, construir primero
docker-compose build
docker-compose -f docker-compose.lightsail.yml up -d
```

## Paso 9: Verificar que Funciona

```bash
# Ver contenedores corriendo
docker ps

# Ver logs
docker-compose -f docker-compose.lightsail.yml logs -f

# Probar desde el servidor
curl http://localhost:8081/api/actuator/health
curl http://localhost
```

## Paso 10: Abrir Puertos en Lightsail

1. En Lightsail Console, ve a tu instancia
2. Pestaña **"Networking"**
3. **"Add rule"**:
   - Application: Custom
   - Protocol: TCP
   - Port: 80
   - Source: Anywhere (0.0.0.0/0)
4. Repetir para puerto 8081

## Acceder desde Navegador

- Frontend: `http://3.232.108.231`
- Backend: `http://3.232.108.231:8081/api`

