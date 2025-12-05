# 🚀 Resumen Rápido: Configurar Acceso desde Mac

## 📝 Pasos Rápidos

### 1️⃣ Desde Windows (Crear Usuario IAM)

```powershell
cd k8s\scripts
.\configurar-acceso-mac.ps1 -Username tu-nombre-mac
```

**Ejemplo:**
```powershell
.\configurar-acceso-mac.ps1 -Username carlos-mac
```

**⚠️ IMPORTANTE:** Guarda las Access Keys que se muestran (solo se muestran una vez).

---

### 2️⃣ Agregar Usuario al Cluster EKS

```powershell
.\agregar-usuario-eks.ps1 -Username tu-nombre-mac
```

Este paso es **CRÍTICO**. Sin esto, no podrás acceder al cluster aunque tengas permisos IAM.

---

### 3️⃣ Desde Mac (Configurar Acceso)

#### Opción A: Script Automático

```bash
cd k8s/scripts
chmod +x configurar-mac.sh
./configurar-mac.sh
```

#### Opción B: Manual

```bash
# 1. Configurar AWS CLI
aws configure --profile vetclinic
# (Ingresa las Access Keys del paso 1)

# 2. Configurar kubectl
aws eks update-kubeconfig \
  --region us-east-1 \
  --name vetclinic-eks \
  --profile vetclinic

# 3. Verificar
kubectl get nodes
kubectl get pods -n vetclinic

# 4. Login a ECR
aws ecr get-login-password --region us-east-1 --profile vetclinic | \
  docker login --username AWS --password-stdin \
  340914758022.dkr.ecr.us-east-1.amazonaws.com
```

---

## ✅ Verificación

```bash
# Verificar AWS
aws sts get-caller-identity --profile vetclinic

# Verificar EKS
kubectl get nodes

# Verificar ECR
aws ecr describe-repositories --profile vetclinic
```

---

## 📚 Documentación Completa

Para más detalles, consulta: `CONFIGURAR-ACCESO-MAC.md`

---

## 🆘 Problemas Comunes

### "User is not authorized"
→ Ejecuta: `.\agregar-usuario-eks.ps1 -Username tu-usuario`

### "Unable to connect to the server"
→ Verifica que el usuario esté en el ConfigMap aws-auth

### "no such host" en ECR
→ Verifica el login a ECR con el comando del paso 3

