# 🍎 Guía para Configurar Acceso AWS desde Mac

Esta guía te ayudará a configurar acceso completo a tu infraestructura AWS (EKS, ECR) desde tu Mac.

## 📋 Prerrequisitos

1. **AWS CLI instalado en Windows** (donde ejecutarás el script de configuración)
2. **Acceso de administrador a la cuenta AWS**
3. **kubectl instalado en Mac** (opcional, se puede instalar después)

---

## 🔧 Paso 1: Crear Usuario IAM y Permisos (Desde Windows)

### Ejecutar el script de configuración:

```powershell
cd k8s\scripts
.\configurar-acceso-mac.ps1 -Username tu-nombre-usuario-mac
```

**Ejemplo:**
```powershell
.\configurar-acceso-mac.ps1 -Username carlos-mac
```

El script:
- ✅ Crea un usuario IAM (o usa uno existente)
- ✅ Crea políticas para EKS y ECR
- ✅ Adjunta las políticas al usuario
- ✅ Genera Access Keys
- ✅ Muestra las credenciales (¡guárdalas!)

### ⚠️ IMPORTANTE: Guardar Credenciales

El script mostrará:
- **Access Key ID**
- **Secret Access Key**

**¡Guarda estas credenciales de forma segura!** El Secret Access Key solo se muestra una vez.

---

## 🍎 Paso 2: Configurar en Mac

### 2.1 Instalar AWS CLI

```bash
# Si tienes Homebrew
brew install awscli

# O descarga desde:
# https://aws.amazon.com/cli/
```

### 2.2 Configurar Credenciales

```bash
aws configure --profile vetclinic
```

Ingresa:
- **AWS Access Key ID**: [La que obtuviste del script]
- **AWS Secret Access Key**: [La que obtuviste del script]
- **Default region name**: `us-east-1`
- **Default output format**: `json`

### 2.3 Verificar Configuración

```bash
aws sts get-caller-identity --profile vetclinic
```

Deberías ver tu usuario IAM.

---

## ☸️ Paso 3: Configurar kubectl en Mac

### 3.1 Instalar kubectl

```bash
# Con Homebrew
brew install kubectl

# O descarga desde:
# https://kubernetes.io/docs/tasks/tools/
```

### 3.2 Configurar Acceso al Cluster EKS

```bash
aws eks update-kubeconfig \
  --region us-east-1 \
  --name vetclinic-eks \
  --profile vetclinic
```

### 3.3 Verificar Acceso

```bash
kubectl get nodes --profile vetclinic
```

Si ves los nodos, ¡estás conectado! 🎉

---

## 🔐 Paso 4: Configurar Acceso en el Cluster (IMPORTANTE)

Aunque tengas permisos en AWS, necesitas agregar tu usuario al ConfigMap `aws-auth` del cluster.

### Opción A: Desde Windows (si tienes acceso)

```powershell
kubectl edit configmap aws-auth -n kube-system
```

Agrega este bloque en la sección `mapUsers`:

```yaml
mapUsers: |
  - userarn: arn:aws:iam::340914758022:user/tu-nombre-usuario-mac
    username: tu-nombre-usuario-mac
    groups:
      - system:masters
```

### Opción B: Script Automático

```powershell
# Desde Windows
cd k8s\scripts
.\agregar-usuario-eks.ps1 -Username tu-nombre-usuario-mac
```

---

## 🐳 Paso 5: Configurar Acceso a ECR

### 5.1 Login a ECR

```bash
aws ecr get-login-password --region us-east-1 --profile vetclinic | \
  docker login --username AWS --password-stdin \
  340914758022.dkr.ecr.us-east-1.amazonaws.com
```

### 5.2 Verificar Repositorios

```bash
aws ecr describe-repositories --profile vetclinic --region us-east-1
```

Deberías ver:
- `vetclinic-backend`
- `vetclinic-frontend`

---

## 🚀 Paso 6: Usar el Despliegue desde Mac

### 6.1 Build y Push de Imágenes

Crea un script `build-and-push.sh` en Mac:

```bash
#!/bin/bash

AWS_ACCOUNT_ID="340914758022"
AWS_REGION="us-east-1"
PROFILE="vetclinic"

# Login a ECR
aws ecr get-login-password --region $AWS_REGION --profile $PROFILE | \
  docker login --username AWS --password-stdin \
  $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build y push backend
docker build -t vetclinic-backend:latest -f Dockerfile .
docker tag vetclinic-backend:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/vetclinic-backend:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/vetclinic-backend:latest

# Build y push frontend
cd frontend-vetclinio-1
docker build -t vetclinic-frontend:latest -f Dockerfile .
docker tag vetclinic-frontend:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/vetclinic-frontend:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/vetclinic-frontend:latest
```

### 6.2 Desplegar en Kubernetes

```bash
# Aplicar cambios
kubectl apply -f k8s/deployment-backend.yaml --profile vetclinic
kubectl apply -f k8s/deployment-frontend.yaml --profile vetclinic

# Ver estado
kubectl get pods -n vetclinic --profile vetclinic
kubectl get services -n vetclinic --profile vetclinic
```

---

## 🔍 Verificar Todo

### Checklist de Verificación:

- [ ] AWS CLI configurado con perfil `vetclinic`
- [ ] `aws sts get-caller-identity` muestra tu usuario
- [ ] `kubectl get nodes` funciona
- [ ] `kubectl get pods -n vetclinic` muestra los pods
- [ ] Login a ECR funciona
- [ ] Puedes hacer push de imágenes a ECR

---

## 🛠️ Comandos Útiles

### Verificar Permisos IAM

```bash
aws iam list-attached-user-policies \
  --user-name tu-nombre-usuario-mac \
  --profile vetclinic
```

### Ver Access Keys

```bash
aws iam list-access-keys \
  --user-name tu-nombre-usuario-mac \
  --profile vetclinic
```

### Cambiar Contexto de kubectl

```bash
# Ver contextos disponibles
kubectl config get-contexts

# Cambiar contexto
kubectl config use-context arn:aws:eks:us-east-1:340914758022:cluster/vetclinic-eks
```

### Ver Logs

```bash
kubectl logs -n vetclinic deployment/backend --profile vetclinic
kubectl logs -n vetclinic deployment/frontend --profile vetclinic
```

---

## 🚨 Solución de Problemas

### Error: "User is not authorized to perform: eks:DescribeCluster"

**Solución:** Verifica que las políticas estén adjuntadas:
```bash
aws iam list-attached-user-policies --user-name tu-usuario --profile vetclinic
```

### Error: "Unable to connect to the server"

**Solución:** 
1. Verifica que el usuario esté en el ConfigMap `aws-auth`:
```bash
kubectl get configmap aws-auth -n kube-system -o yaml
```

2. Si no está, agrégalo (ver Paso 4).

### Error: "no such host" al hacer push a ECR

**Solución:** Verifica el login a ECR:
```bash
aws ecr get-login-password --region us-east-1 --profile vetclinic | \
  docker login --username AWS --password-stdin \
  340914758022.dkr.ecr.us-east-1.amazonaws.com
```

---

## 📝 Notas Importantes

1. **Seguridad:** Nunca compartas tus Access Keys. Si se comprometen, elimínalas inmediatamente.
2. **Múltiples Perfiles:** Puedes tener varios perfiles AWS en tu Mac usando `--profile`.
3. **ConfigMap aws-auth:** Este es el paso más importante. Sin esto, no podrás acceder al cluster aunque tengas permisos IAM.
4. **Región:** Asegúrate de usar `us-east-1` en todos los comandos.

---

## ✅ Resumen Rápido

```bash
# 1. Configurar AWS CLI
aws configure --profile vetclinic

# 2. Configurar kubectl
aws eks update-kubeconfig --region us-east-1 --name vetclinic-eks --profile vetclinic

# 3. Verificar
kubectl get nodes --profile vetclinic

# 4. Login ECR
aws ecr get-login-password --region us-east-1 --profile vetclinic | \
  docker login --username AWS --password-stdin \
  340914758022.dkr.ecr.us-east-1.amazonaws.com
```

¡Listo! Ya tienes acceso completo desde tu Mac. 🎉

