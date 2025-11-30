# ✅ Siguientes Pasos en Lightsail

## Paso 1: Verificar Docker Compose

```bash
docker-compose --version
```

Si no está instalado:
```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

## Paso 2: Subir tu Aplicación

Tienes 2 opciones:

### Opción A: Clonar desde Git (Recomendado)

```bash
# Instalar Git
sudo apt install -y git

# Clonar tu repositorio
git clone <tu-repositorio-backend> backend-vetclinic-1
cd backend-vetclinic-1
```

### Opción B: Subir el archivo .tar

**Desde tu máquina Windows (PowerShell):**
```powershell
cd C:\Users\ByArc\Desktop\backend-vetclinic-1
scp -i $env:USERPROFILE\Downloads\VetClinic.pem vetclinic-images.tar ubuntu@3.232.108.231:/home/ubuntu/
```

**En el servidor:**
```bash
# Cargar imágenes
docker load -i vetclinic-images.tar

# Verificar
docker images
```

## Paso 3: Subir archivos necesarios

Si usaste Git, ya tienes todo. Si usaste .tar, necesitas subir los archivos de configuración:

```bash
# Crear directorio
mkdir -p ~/backend-vetclinic-1
cd ~/backend-vetclinic-1
```

**Desde tu máquina, subir archivos:**
```powershell
# Subir docker-compose.lightsail.yml
scp -i $env:USERPROFILE\Downloads\VetClinic.pem docker-compose.lightsail.yml ubuntu@3.232.108.231:/home/ubuntu/backend-vetclinic-1/

# Subir env.example
scp -i $env:USERPROFILE\Downloads\VetClinic.pem env.example ubuntu@3.232.108.231:/home/ubuntu/backend-vetclinic-1/
```

## Paso 4: Configurar Variables de Entorno

```bash
cd ~/backend-vetclinic-1

# Copiar ejemplo
cp env.example .env

# Editar con tus valores
nano .env
```

Ajusta estos valores importantes:
- `POSTGRES_PASSWORD` - Contraseña segura para PostgreSQL
- `JWT_SECRET` - Clave secreta para JWT (genera una segura)
- `MAIL_USERNAME` y `MAIL_PASSWORD` - Credenciales de Gmail
- `FRONTEND_URL` y `BACKEND_URL` - Usa tu IP: `http://3.232.108.231`
- `CORS_ORIGINS` - `http://3.232.108.231`

Guardar: `Ctrl+O`, Enter, `Ctrl+X`

## Paso 5: Ejecutar la Aplicación

```bash
# Si cargaste el .tar
docker-compose -f docker-compose.lightsail.yml up -d

# Si clonaste el repo, construir primero
docker-compose build
docker-compose -f docker-compose.lightsail.yml up -d
```

## Paso 6: Verificar que Funciona

```bash
# Ver contenedores
docker ps

# Ver logs
docker-compose -f docker-compose.lightsail.yml logs -f

# Probar desde el servidor
curl http://localhost:8081/api/actuator/health
curl http://localhost
```

## Paso 7: Abrir Puertos en Lightsail

1. Ve a Lightsail Console
2. Tu instancia → Pestaña "Networking"
3. "Add rule":
   - Application: Custom
   - Protocol: TCP
   - Port: 80
   - Source: Anywhere (0.0.0.0/0)
4. Repetir para puerto 8081

## Acceder desde Navegador

- Frontend: `http://3.232.108.231`
- Backend: `http://3.232.108.231:8081/api`

