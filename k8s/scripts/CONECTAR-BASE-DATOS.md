# 📊 Guía para Visualizar la Base de Datos PostgreSQL

Esta guía te muestra cómo conectarte y visualizar tu base de datos PostgreSQL desplegada en Kubernetes.

## 🚀 Opción 1: Usar el Script Automatizado (Recomendado)

### Ver información de conexión:
```powershell
cd k8s\scripts
.\connect-database.ps1 -Action info
```

### Hacer Port Forward (para herramientas gráficas):
```powershell
.\connect-database.ps1 -Action port-forward
```

### Conectar directamente con psql:
```powershell
.\connect-database.ps1 -Action psql
```

## 🔧 Opción 2: Comandos Manuales

### 1. Obtener el nombre del pod:
```powershell
kubectl get pods -n vetclinic -l app=postgres
```

### 2. Obtener la contraseña:
```powershell
kubectl get secret backend-secrets -n vetclinic -o jsonpath='{.data.DATABASE_PASSWORD}' | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }
```

### 3. Hacer Port Forward:
```powershell
kubectl port-forward -n vetclinic svc/postgres-service 5432:5432
```

### 4. Conectar con psql (en otra terminal):
```powershell
# Obtener nombre del pod primero
$pod = kubectl get pods -n vetclinic -l app=postgres -o jsonpath='{.items[0].metadata.name}'

# Conectar
kubectl exec -it -n vetclinic $pod -- psql -U postgres -d vetclinic_db
```

## 🎨 Opción 3: Herramientas Gráficas (pgAdmin, DBeaver, etc.)

### Paso 1: Iniciar Port Forward
```powershell
kubectl port-forward -n vetclinic svc/postgres-service 5432:5432
```

**⚠️ IMPORTANTE:** Deja esta terminal abierta mientras uses la herramienta gráfica.

### Paso 2: Configurar la conexión en tu herramienta

#### Para pgAdmin:
- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `vetclinic_db`
- **Username:** `postgres`
- **Password:** [Obtener del secret con el comando de arriba]

#### Para DBeaver:
- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `vetclinic_db`
- **Username:** `postgres`
- **Password:** [Obtener del secret con el comando de arriba]

#### Para DataGrip:
- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `vetclinic_db`
- **User:** `postgres`
- **Password:** [Obtener del secret con el comando de arriba]

#### Para TablePlus:
- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `vetclinic_db`
- **User:** `postgres`
- **Password:** [Obtener del secret con el comando de arriba]

## 📋 Información de la Base de Datos

- **Namespace:** `vetclinic`
- **Service:** `postgres-service`
- **Database:** `vetclinic_db`
- **User:** `postgres`
- **Port:** `5432`
- **Password:** Almacenada en el secret `backend-secrets`

## 🔍 Comandos Útiles de PostgreSQL

Una vez conectado con psql, puedes usar estos comandos:

```sql
-- Listar todas las tablas
\dt

-- Describir una tabla
\d nombre_tabla

-- Ver todas las bases de datos
\l

-- Ver todos los esquemas
\dn

-- Ver todos los usuarios
\du

-- Ejecutar una consulta
SELECT * FROM nombre_tabla LIMIT 10;

-- Salir de psql
\q
```

## 🛠️ Solución de Problemas

### Error: "No se encontró el pod"
```powershell
# Verificar que el pod esté corriendo
kubectl get pods -n vetclinic -l app=postgres

# Ver logs del pod si hay problemas
kubectl logs -n vetclinic -l app=postgres
```

### Error: "No se pudo obtener la contraseña"
```powershell
# Verificar que el secret exista
kubectl get secrets -n vetclinic

# Ver el contenido del secret (sin decodificar)
kubectl get secret backend-secrets -n vetclinic -o yaml
```

### Error: "Port 5432 already in use"
```powershell
# Usar un puerto diferente
kubectl port-forward -n vetclinic svc/postgres-service 5433:5432

# Luego conectar a localhost:5433 en tu herramienta
```

## 📝 Notas Importantes

1. **Port Forward es temporal:** Cuando cierres la terminal, se perderá la conexión.
2. **Seguridad:** El port forward solo funciona localmente, no expone la base de datos a internet.
3. **Performance:** Para producción, considera usar herramientas de monitoreo como Prometheus + Grafana.
4. **Backups:** Asegúrate de tener backups regulares de tu base de datos.

## 🔐 Obtener Contraseña Rápido

```powershell
# Comando rápido para obtener la contraseña
kubectl get secret backend-secrets -n vetclinic -o jsonpath='{.data.DATABASE_PASSWORD}' | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) } | clip

# La contraseña se copió al portapapeles, solo pégalo donde la necesites
```

