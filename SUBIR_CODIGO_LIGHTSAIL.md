# 📤 Guía para Subir Código Fuente a Lightsail

## Archivos que Necesitas Subir

### Backend:
- Todo el directorio `backend-vetclinic-1/`
- Incluye: Dockerfile, docker-compose.lightsail.yml, src/, pom.xml, etc.

### Frontend:
- Todo el directorio `frontend-vetclinio-1/` (que está fuera del backend)

## Paso 1: Comprimir los Archivos (Opcional pero Recomendado)

**En tu máquina Windows (PowerShell):**

```powershell
# Ir al escritorio
cd C:\Users\ByArc\Desktop

# Comprimir backend
Compress-Archive -Path backend-vetclinic-1 -DestinationPath backend-vetclinic-1.zip -Force

# Comprimir frontend
Compress-Archive -Path frontend-vetclinio-1 -DestinationPath frontend-vetclinio-1.zip -Force
```

## Paso 2: Subir Backend

### Opción A: Con SCP (directo)

```powershell
cd C:\Users\ByArc\Desktop
scp -i $env:USERPROFILE\Downloads\VetClinic.pem -r backend-vetclinic-1 ubuntu@3.232.108.231:/home/ubuntu/
```

### Opción B: Con ZIP (más rápido)

```powershell
# Subir el zip
scp -i $env:USERPROFILE\Downloads\VetClinic.pem backend-vetclinic-1.zip ubuntu@3.232.108.231:/home/ubuntu/

# En el servidor, descomprimir:
# unzip backend-vetclinic-1.zip
```

## Paso 3: Subir Frontend

```powershell
# Desde tu máquina
scp -i $env:USERPROFILE\Downloads\VetClinic.pem -r frontend-vetclinio-1 ubuntu@3.232.108.231:/home/ubuntu/
```

O si usaste ZIP:
```powershell
scp -i $env:USERPROFILE\Downloads\VetClinic.pem frontend-vetclinio-1.zip ubuntu@3.232.108.231:/home/ubuntu/
```

## Paso 4: En el Servidor - Preparar Archivos

```bash
# Verificar que se subieron
ls -la

# Si subiste ZIPs, descomprimir
sudo apt install -y unzip
unzip backend-vetclinic-1.zip
unzip frontend-vetclinio-1.zip

# Ir al directorio del backend
cd backend-vetclinic-1

# Verificar estructura
ls -la
```

## Paso 5: Actualizar docker-compose.lightsail.yml

Necesitas asegurarte de que el archivo `docker-compose.lightsail.yml` tenga la ruta correcta del frontend.

Si el frontend está en `/home/ubuntu/frontend-vetclinio-1`, actualiza el archivo:

```bash
nano docker-compose.lightsail.yml
```

Busca la sección `frontend:` y cambia:
```yaml
frontend:
  build:
    context: ../frontend-vetclinio-1  # ← Ajusta esta ruta
```

## Paso 6: Configurar Variables de Entorno

```bash
# Crear .env
cp env.example .env
nano .env
```

Ajusta los valores (especialmente las URLs con tu IP: 3.232.108.231)

## Paso 7: Construir y Ejecutar

```bash
# Construir imágenes
docker-compose -f docker-compose.lightsail.yml build

# Ejecutar
docker-compose -f docker-compose.lightsail.yml up -d

# Ver logs
docker-compose -f docker-compose.lightsail.yml logs -f
```

