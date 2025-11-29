# Guía de Despliegue en AWS

Esta guía te ayudará a desplegar VetClinic Pro en AWS usando los servicios más adecuados.

## Arquitectura Recomendada

```
Internet
    ↓
CloudFront (CDN) / ALB (Application Load Balancer)
    ↓
ECS Fargate (Frontend + Backend)
    ↓
RDS PostgreSQL (Base de datos)
    ↓
SES (Simple Email Service) / SNS (SMS)
```

## Opción 1: AWS ECS (Elastic Container Service) - RECOMENDADO

### Ventajas:
- ✅ Escalado automático
- ✅ Gestión de contenedores sin servidores
- ✅ Integración con otros servicios AWS
- ✅ Fácil actualización y despliegue

### Pasos:

#### 1. Preparar Imágenes Docker

```bash
# Construir imágenes
docker build -t vetclinic-backend:latest .
docker build -t vetclinic-frontend:latest ./frontend-vetclinio-1

# Etiquetar para ECR
docker tag vetclinic-backend:latest <tu-account-id>.dkr.ecr.<region>.amazonaws.com/vetclinic-backend:latest
docker tag vetclinic-frontend:latest <tu-account-id>.dkr.ecr.<region>.amazonaws.com/vetclinic-frontend:latest
```

#### 2. Crear ECR (Elastic Container Registry)

```bash
# Crear repositorios
aws ecr create-repository --repository-name vetclinic-backend --region us-east-1
aws ecr create-repository --repository-name vetclinic-frontend --region us-east-1

# Autenticarse
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <tu-account-id>.dkr.ecr.us-east-1.amazonaws.com

# Subir imágenes
docker push <tu-account-id>.dkr.ecr.us-east-1.amazonaws.com/vetclinic-backend:latest
docker push <tu-account-id>.dkr.ecr.us-east-1.amazonaws.com/vetclinic-frontend:latest
```

#### 3. Crear RDS PostgreSQL

```bash
# Crear instancia RDS
aws rds create-db-instance \
    --db-instance-identifier vetclinic-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --master-username postgres \
    --master-user-password <tu-password-segura> \
    --allocated-storage 20 \
    --vpc-security-group-ids <security-group-id> \
    --db-subnet-group-name <subnet-group> \
    --backup-retention-period 7 \
    --region us-east-1
```

**Nota:** Guarda el endpoint de RDS para configurarlo en ECS.

#### 4. Crear Task Definitions para ECS

Crea dos task definitions (una para backend, otra para frontend) o usa una con múltiples contenedores.

**Backend Task Definition** (`task-definition-backend.json`):
```json
{
  "family": "vetclinic-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "vetclinic-backend",
      "image": "<account-id>.dkr.ecr.<region>.amazonaws.com/vetclinic-backend:latest",
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "DATABASE_URL",
          "value": "jdbc:postgresql://<rds-endpoint>:5432/vetclinic_db"
        },
        {
          "name": "DATABASE_USERNAME",
          "value": "postgres"
        },
        {
          "name": "JWT_SECRET",
          "value": "<tu-jwt-secret>"
        }
      ],
      "secrets": [
        {
          "name": "DATABASE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:<region>:<account-id>:secret:vetclinic/db-password"
        },
        {
          "name": "MAIL_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:<region>:<account-id>:secret:vetclinic/mail-password"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/vetclinic-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8081/api/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 40
      }
    }
  ]
}
```

#### 5. Crear ECS Cluster y Services

```bash
# Crear cluster
aws ecs create-cluster --cluster-name vetclinic-cluster --region us-east-1

# Registrar task definition
aws ecs register-task-definition --cli-input-json file://task-definition-backend.json

# Crear service
aws ecs create-service \
    --cluster vetclinic-cluster \
    --service-name vetclinic-backend \
    --task-definition vetclinic-backend \
    --desired-count 2 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" \
    --load-balancers "targetGroupArn=<target-group-arn>,containerName=vetclinic-backend,containerPort=8081"
```

#### 6. Configurar Application Load Balancer (ALB)

```bash
# Crear ALB
aws elbv2 create-load-balancer \
    --name vetclinic-alb \
    --subnets subnet-xxx subnet-yyy \
    --security-groups sg-xxx

# Crear target groups para backend y frontend
aws elbv2 create-target-group \
    --name vetclinic-backend-tg \
    --protocol HTTP \
    --port 8081 \
    --vpc-id vpc-xxx \
    --health-check-path /api/actuator/health

# Crear listener
aws elbv2 create-listener \
    --load-balancer-arn <alb-arn> \
    --protocol HTTPS \
    --port 443 \
    --certificates CertificateArn=<cert-arn> \
    --default-actions Type=forward,TargetGroupArn=<target-group-arn>
```

#### 7. Configurar Secrets Manager

```bash
# Crear secretos
aws secretsmanager create-secret \
    --name vetclinic/db-password \
    --secret-string "tu-password-segura"

aws secretsmanager create-secret \
    --name vetclinic/mail-password \
    --secret-string "tu-app-password-gmail"
```

## Opción 2: AWS Elastic Beanstalk - MÁS SIMPLE

### Ventajas:
- ✅ Más fácil de configurar
- ✅ Gestión automática de infraestructura
- ✅ Bueno para empezar

### Pasos:

1. **Instalar EB CLI**
```bash
pip install awsebcli
```

2. **Inicializar Elastic Beanstalk**
```bash
cd backend-vetclinic-1
eb init -p docker vetclinic-backend --region us-east-1
eb create vetclinic-backend-env
```

3. **Configurar variables de entorno**
```bash
eb setenv SPRING_PROFILES_ACTIVE=prod \
          DATABASE_URL=jdbc:postgresql://<rds-endpoint>:5432/vetclinic_db \
          JWT_SECRET=<tu-secret>
```

## Opción 3: AWS Lightsail - MÁS ECONÓMICO

### Ventajas:
- ✅ Precio fijo y predecible
- ✅ Fácil de configurar
- ✅ Bueno para proyectos pequeños/medianos

### Pasos:

1. Crear instancia Lightsail con Docker
2. Conectar por SSH
3. Clonar repositorio
4. Configurar docker-compose
5. Ejecutar `docker-compose -f docker-compose.prod.yml up -d`

## Configuración de Dominio

### Usando Route 53:

1. Registrar dominio o usar existente
2. Crear hosted zone
3. Crear registros A/AAAA apuntando al ALB o IP de Lightsail
4. Configurar certificado SSL con ACM (AWS Certificate Manager)

## Monitoreo y Logs

### CloudWatch:
- Configurar log groups para ECS
- Crear dashboards
- Configurar alarmas

### X-Ray (Opcional):
- Para tracing distribuido
- Útil para debugging

## Costos Estimados (Mensual)

### ECS Fargate:
- Backend: ~$50-100 (2 vCPU, 4GB RAM)
- Frontend: ~$20-40 (1 vCPU, 2GB RAM)
- RDS: ~$15-30 (db.t3.micro)
- ALB: ~$20
- **Total: ~$105-190/mes**

### Lightsail:
- Instancia: ~$10-20/mes
- RDS: ~$15-30/mes
- **Total: ~$25-50/mes**

## Checklist de Despliegue

- [ ] Imágenes Docker construidas y probadas localmente
- [ ] Repositorios ECR creados
- [ ] Imágenes subidas a ECR
- [ ] RDS PostgreSQL creado y configurado
- [ ] Security Groups configurados correctamente
- [ ] Variables de entorno configuradas
- [ ] Secrets Manager configurado
- [ ] ALB configurado con SSL
- [ ] Dominio configurado
- [ ] Health checks funcionando
- [ ] Logs configurados en CloudWatch
- [ ] Backup de base de datos configurado
- [ ] Monitoreo y alarmas configurados

## Comandos Útiles

```bash
# Ver logs de ECS
aws logs tail /ecs/vetclinic-backend --follow

# Actualizar service
aws ecs update-service --cluster vetclinic-cluster --service vetclinic-backend --force-new-deployment

# Escalar service
aws ecs update-service --cluster vetclinic-cluster --service vetclinic-backend --desired-count 3

# Ver estado de tasks
aws ecs list-tasks --cluster vetclinic-cluster --service-name vetclinic-backend
```

## Seguridad

1. **Nunca** commits credenciales al repositorio
2. Usa Secrets Manager para passwords
3. Configura Security Groups restrictivamente
4. Usa HTTPS siempre
5. Habilita WAF si es necesario
6. Configura backups automáticos de RDS

## Soporte

Para más información:
- [Documentación de ECS](https://docs.aws.amazon.com/ecs/)
- [Documentación de RDS](https://docs.aws.amazon.com/rds/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)

