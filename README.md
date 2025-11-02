# 🏥 VetClinic Pro - Backend API# VetClinic Pro - Backend API



API RESTful para el sistema integral de gestión de clínicas veterinarias - Backend desarrollado con Spring Boot 3.Backend REST API para el Sistema de Gestión de Clínica Veterinaria desarrollado con Java Spring Boot.



## 🚀 Características## 🚀 Tecnologías



- 🔐 **Autenticación JWT**: Sistema seguro con access y refresh tokens- **Java 17**

- 👥 **Gestión de Usuarios**: CRUD completo con roles y permisos- **Spring Boot 3.2.0**

- 🎯 **RBAC**: Control de acceso basado en roles (Role-Based Access Control)- **Spring Security** + JWT

- 📊 **Dashboard**: Estadísticas y métricas en tiempo real- **Spring Data JPA** + Hibernate

- 🏗️ **Arquitectura Limpia**: Separación de capas (Controller, Service, Repository)- **PostgreSQL**

- 🎨 **Patrones de Diseño**: 15+ patrones implementados- **Maven**

- 🔒 **Seguridad**: BCrypt, JWT HS512, Spring Security- **Swagger/OpenAPI** para documentación

- 📝 **Validación**: Bean Validation con mensajes personalizados- **Lombok** para reducir boilerplate

- 🐳 **Docker**: Containerización completa con Docker Compose- **MapStruct** para mapeo de objetos



## 🛠️ Stack Tecnológico## 📋 Requisitos Previos



- **Java 17** - LTS- JDK 17 o superior

- **Spring Boot 3.2.0** - Framework principal- Maven 3.8+

- **Spring Security** - Autenticación y autorización- PostgreSQL 14+

- **Spring Data JPA** - Persistencia de datos- (Opcional) Docker y Docker Compose

- **PostgreSQL 15** - Base de datos relacional

- **JWT (JJWT 0.12.3)** - JSON Web Tokens## 🔧 Configuración

- **Lombok** - Reducción de boilerplate

- **Maven** - Gestión de dependencias### 1. Base de Datos

- **Docker & Docker Compose** - Containerización

Crear base de datos PostgreSQL:

## 📋 Requisitos Previos

```sql

- Java 17 o superiorCREATE DATABASE vetclinic_db;

- Maven 3.8+CREATE USER vetclinic_user WITH PASSWORD 'vetclinic_pass';

- PostgreSQL 15+ (o Docker)GRANT ALL PRIVILEGES ON DATABASE vetclinic_db TO vetclinic_user;

- Git```



## 🔧 Instalación y Configuración### 2. Configuración de Aplicación



### 1. Clonar el repositorioEditar `src/main/resources/application.yml`:



```bash```yaml

git clone https://github.com/Carlos1AB1/backend-vetclinic.gitspring:

cd backend-vetclinic  datasource:

```    url: jdbc:postgresql://localhost:5432/vetclinic_db

    username: vetclinic_user

### 2. Configurar Base de Datos    password: vetclinic_pass



**Opción A: Usar Docker Compose (Recomendado)**jwt:

  secret: your-secret-key-here-min-256-bits

```bash  

docker-compose up -dspring:

```  mail:

    username: your-email@gmail.com

Esto levantará PostgreSQL en el puerto 5432.    password: your-app-password

```

**Opción B: PostgreSQL Local**

### 3. Variables de Entorno (Recomendado para Producción)

```sql

CREATE DATABASE vetclinic_db;```bash

CREATE USER vetclinic_user WITH PASSWORD 'vetclinic_pass';export DATABASE_URL=jdbc:postgresql://localhost:5432/vetclinic_db

GRANT ALL PRIVILEGES ON DATABASE vetclinic_db TO vetclinic_user;export DATABASE_USERNAME=vetclinic_user

```export DATABASE_PASSWORD=vetclinic_pass

export JWT_SECRET=your-256-bit-secret

### 3. Configurar Variables de Entornoexport MAIL_USERNAME=your-email@gmail.com

export MAIL_PASSWORD=your-app-password

Editar `src/main/resources/application.yml` o crear `application-dev.yml`:export FRONTEND_URL=http://localhost:5173

```

```yaml

spring:## 🏃 Ejecutar la Aplicación

  datasource:

    url: jdbc:postgresql://localhost:5432/vetclinic_db### Modo Desarrollo

    username: vetclinic_user

    password: vetclinic_pass```bash

  jpa:cd backend

    hibernate:mvn spring-boot:run

      ddl-auto: update```

    show-sql: true

### Compilar y Ejecutar

jwt:

  secret: tu-clave-secreta-de-al-menos-64-caracteres-para-HS512```bash

  expiration: 86400000  # 24 horasmvn clean package

  refresh-expiration: 604800000  # 7 díasjava -jar target/vetclinic-backend-1.0.0.jar

``````



### 4. Compilar y Ejecutar### Con Docker



```bash```bash

# Compilardocker-compose up -d

mvn clean install```



# Ejecutar en desarrollo## 📚 Documentación API

mvn spring-boot:run

Una vez iniciada la aplicación, acceder a:

# O con perfil específico

mvn spring-boot:run -Dspring-boot.run.profiles=dev- **Swagger UI**: http://localhost:8080/swagger-ui.html

```- **OpenAPI JSON**: http://localhost:8080/api-docs



La API estará disponible en: `http://localhost:8081/api`## 🔐 Autenticación



## 📁 Estructura del ProyectoLa API utiliza JWT (JSON Web Tokens) para autenticación.



```### Login

backend-vetclinic/

├── src/```bash

│   ├── main/POST /auth/login

│   │   ├── java/com/vetclinic/Content-Type: application/json

│   │   │   ├── config/                 # Configuraciones

│   │   │   │   ├── CorsConfig.java{

│   │   │   │   ├── DataInitializer.java  "username": "admin",

│   │   │   │   ├── JacksonConfig.java  "password": "admin123"

│   │   │   │   └── SecurityConfig.java}

│   │   │   ├── controller/             # Controladores REST```

│   │   │   │   ├── AuthController.java

│   │   │   │   ├── DashboardController.java### Usar Token

│   │   │   │   └── UserController.java

│   │   │   ├── dto/                    # Data Transfer Objects```bash

│   │   │   │   ├── auth/Authorization: Bearer <your-jwt-token>

│   │   │   │   ├── dashboard/```

│   │   │   │   └── user/

│   │   │   ├── entity/                 # Entidades JPA## 👤 Usuario por Defecto

│   │   │   │   ├── User.java

│   │   │   │   ├── Role.javaAl iniciar la aplicación por primera vez, se crea un usuario administrador:

│   │   │   │   ├── Permission.java

│   │   │   │   └── PasswordResetToken.java- **Username**: `admin`

│   │   │   ├── exception/              # Manejo de excepciones- **Password**: `admin123`

│   │   │   │   ├── GlobalExceptionHandler.java- **Email**: `admin@vetclinic.com`

│   │   │   │   └── custom exceptions...

│   │   │   ├── patterns/               # Patrones de diseño⚠️ **IMPORTANTE**: Cambiar la contraseña después del primer login.

│   │   │   │   ├── adapter/

│   │   │   │   ├── builder/## 📁 Estructura del Proyecto

│   │   │   │   ├── chain/

│   │   │   │   ├── decorator/```

│   │   │   │   ├── factory/backend/

│   │   │   │   ├── observer/├── src/

│   │   │   │   ├── singleton/│   ├── main/

│   │   │   │   └── strategy/│   │   ├── java/com/vetclinic/

│   │   │   ├── repository/             # Repositorios JPA│   │   │   ├── config/              # Configuraciones

│   │   │   │   ├── UserRepository.java│   │   │   ├── controller/          # REST Controllers

│   │   │   │   ├── RoleRepository.java│   │   │   ├── dto/                 # Data Transfer Objects

│   │   │   │   └── PermissionRepository.java│   │   │   ├── entity/              # Entidades JPA

│   │   │   ├── security/               # Seguridad JWT│   │   │   ├── exception/           # Excepciones personalizadas

│   │   │   │   ├── JwtAuthenticationFilter.java│   │   │   ├── patterns/            # Patrones de diseño

│   │   │   │   ├── JwtTokenProvider.java│   │   │   │   ├── adapter/         # Adapter Pattern

│   │   │   │   └── CustomUserDetailsService.java│   │   │   │   ├── chain/           # Chain of Responsibility

│   │   │   ├── service/                # Servicios de negocio│   │   │   │   └── strategy/        # Strategy Pattern

│   │   │   │   ├── AuthService.java│   │   │   ├── repository/          # Repositorios JPA

│   │   │   │   ├── DashboardService.java│   │   │   ├── security/            # Seguridad y JWT

│   │   │   │   ├── UserService.java│   │   │   ├── service/             # Lógica de negocio

│   │   │   │   └── RoleService.java│   │   │   └── VetClinicApplication.java

│   │   │   └── VetClinicApplication.java│   │   └── resources/

│   │   └── resources/│   │       ├── application.yml

│   │       ├── application.yml│   │       ├── application-dev.yml

│   │       ├── application-dev.yml│   │       └── application-prod.yml

│   │       ├── application-prod.yml│   └── test/                        # Tests

│   │       └── schema.sql├── pom.xml

│   └── test/└── README.md

│       └── java/com/vetclinic/```

├── target/

├── docker-compose.yml## 🎨 Patrones de Diseño Implementados

├── Dockerfile

├── pom.xml### 1. Singleton

└── README.md- `JwtTokenProvider`: Instancia única para gestión de JWT

```

### 2. Chain of Responsibility

## 🎯 Módulos Implementados (DEV 1)- `ValidationHandler`: Cadena de validaciones

- `RegistrationValidationChain`: Validación de registro

### ✅ Módulo de Autenticación

- Login con JWT### 3. Strategy

- Refresh Token- `NotificationStrategy`: Diferentes canales de notificación

- Logout- `EmailNotificationStrategy`, `SmsNotificationStrategy`

- Registro de usuarios

### 4. Adapter

**Branch**: `feature/auth-module`- `EmailServiceAdapter`: Adaptador para servicios de email

- `SpringMailAdapter`: Implementación con Spring Mail

### ✅ Módulo de Usuarios

- CRUD completo## 🔒 Seguridad Implementada

- Gestión de roles y permisos

- Validación de datos- ✅ Autenticación JWT

- Búsqueda y filtrado- ✅ Autorización basada en roles

- ✅ Control de permisos granular

**Branch**: `feature/user-management`- ✅ Encriptación de contraseñas (BCrypt)

- ✅ Protección contra intentos de login

### ✅ Módulo de Dashboard- ✅ Bloqueo automático de cuentas

- Estadísticas generales- ✅ Tokens de recuperación de contraseña

- Métricas en tiempo real- ✅ CORS configurado

- Resumen de actividades

## 📊 Endpoints Principales

**Branch**: `feature/dashboard`

### Autenticación

### 🔄 Módulos Pendientes (Para otros DEVs)- `POST /auth/login` - Iniciar sesión

- `POST /auth/register` - Registrar usuario

- **Módulo de Pacientes** → `feature/patient-management`- `POST /auth/refresh-token` - Refrescar token

- **Módulo de Propietarios** → `feature/owner-management`- `POST /auth/forgot-password` - Solicitar reset de contraseña

- **Módulo de Citas** → `feature/appointment-management`- `POST /auth/reset-password` - Resetear contraseña

- **Módulo de Historial Médico** → `feature/medical-records`- `POST /auth/change-password` - Cambiar contraseña

- **Módulo de Inventario** → `feature/inventory-management`- `GET /auth/me` - Obtener usuario actual

- **Módulo de Reportes** → `feature/reports`

### Usuarios (Admin)

## 🔐 Seguridad- `GET /users` - Listar usuarios

- `GET /users/{id}` - Obtener usuario

### Autenticación JWT- `PUT /users/{id}` - Actualizar usuario

- `DELETE /users/{id}` - Eliminar usuario

```- `POST /users/{id}/unlock` - Desbloquear usuario

POST /api/auth/login

{## 🧪 Testing

  "username": "admin",

  "password": "admin123"```bash

}# Ejecutar tests

mvn test

Response:

{# Con cobertura

  "accessToken": "eyJhbGc...",mvn test jacoco:report

  "refreshToken": "eyJhbGc...",```

  "tokenType": "Bearer",

  "expiresIn": 86400000## 🚀 Despliegue

}

```### Producción



### Uso del Token1. Configurar variables de entorno

2. Usar perfil de producción:

Incluir en el header de cada petición:

``````bash

Authorization: Bearer {accessToken}java -jar -Dspring.profiles.active=prod target/vetclinic-backend-1.0.0.jar

``````



### Roles Disponibles### Docker



- **ADMIN**: Acceso total al sistema```bash

- **VETERINARIAN**: Gestión de pacientes y registros médicosdocker build -t vetclinic-backend .

- **RECEPTIONIST**: Gestión de citas y propietariosdocker run -p 8080:8080 --env-file .env vetclinic-backend

```

## 📡 Endpoints Principales

## 📝 Logs

### Autenticación

Los logs se guardan en:

| Método | Endpoint | Descripción |- Consola (development)

|--------|----------|-------------|- `logs/vetclinic.log` (producción)

| POST | `/api/auth/login` | Iniciar sesión |

| POST | `/api/auth/refresh` | Renovar token |## 🛠️ Desarrollo

| POST | `/api/auth/logout` | Cerrar sesión |

### Agregar Nueva Funcionalidad

### Usuarios (Requiere ADMIN)

1. Crear entity en `entity/`

| Método | Endpoint | Descripción |2. Crear repository en `repository/`

|--------|----------|-------------|3. Crear DTOs en `dto/`

| GET | `/api/users` | Listar usuarios |4. Implementar service en `service/`

| GET | `/api/users/{id}` | Obtener usuario |5. Crear controller en `controller/`

| POST | `/api/users` | Crear usuario |6. Agregar tests

| PUT | `/api/users/{id}` | Actualizar usuario |

| DELETE | `/api/users/{id}` | Eliminar usuario |### Hot Reload



### DashboardEl proyecto incluye Spring DevTools para hot reload en desarrollo.



| Método | Endpoint | Descripción |## 🐛 Troubleshooting

|--------|----------|-------------|

| GET | `/api/dashboard/stats` | Estadísticas generales |### Error de Conexión a BD

- Verificar que PostgreSQL esté corriendo

## 🎨 Patrones de Diseño Implementados- Verificar credenciales en `application.yml`



1. **Chain of Responsibility**: Validación en cadena de datos### Error JWT Invalid

2. **Adapter Pattern**: Integración con servicios externos- Verificar que el secret tenga al menos 256 bits

3. **Strategy Pattern**: Estrategias de notificación- Verificar que el token no haya expirado

4. **Builder Pattern**: Construcción de DTOs complejos

5. **Singleton Pattern**: Gestión de configuraciones### Email no se envía

6. **Repository Pattern**: Abstracción de persistencia- Verificar configuración SMTP

7. **Facade Pattern**: Simplificación de operaciones complejas- Para Gmail, usar App Password

8. **DTO Pattern**: Transferencia de datos

9. **Factory Pattern**: Creación de objetos## 📞 Soporte

10. **Decorator Pattern**: Extensión de funcionalidades

11. **Observer Pattern**: Sistema de eventosPara problemas o preguntas:

12. **Dependency Injection**: Inversión de control (Spring)- Email: support@vetclinic.com

13. **MVC Pattern**: Arquitectura Model-View-Controller- Issues: GitHub Issues

14. **Service Layer Pattern**: Lógica de negocio

15. **Filter/Interceptor Pattern**: JWT Authentication Filter## 📄 Licencia



## 🌿 Estrategia de BranchingMIT License - Ver LICENSE file para más detalles



### Branches Principales## 👨‍💻 Autor



- **`main`**: Código en producción (estable)VetClinic Pro Development Team

- **`develop`**: Rama de desarrollo (integración)

---

### Branches de Features

**Versión**: 1.0.0  

```**Última Actualización**: Noviembre 2025

feature/auth-module          # Autenticación
feature/user-management      # Gestión de usuarios
feature/dashboard            # Dashboard y estadísticas
feature/patient-management   # Pacientes (pendiente)
feature/owner-management     # Propietarios (pendiente)
feature/appointment-management # Citas (pendiente)
feature/medical-records      # Historiales médicos (pendiente)
feature/inventory-management # Inventario (pendiente)
```

### Workflow de Git

```bash
# 1. Crear branch desde develop
git checkout develop
git pull origin develop
git checkout -b feature/nombre-modulo

# 2. Trabajar en el módulo
git add .
git commit -m "feat(modulo): descripción del cambio"

# 3. Push del branch
git push -u origin feature/nombre-modulo

# 4. Crear Pull Request a develop
# Revisar código y hacer merge

# 5. Cuando develop esté estable, merge a main
```

### Convención de Commits

```
feat(modulo): Nueva funcionalidad
fix(modulo): Corrección de bug
docs(modulo): Documentación
refactor(modulo): Refactorización
test(modulo): Tests
chore(modulo): Tareas de mantenimiento
```

## 🐳 Docker

### Levantar con Docker Compose

```bash
docker-compose up -d
```

### Build Manual

```bash
# Build de la aplicación
mvn clean package -DskipTests

# Build de la imagen Docker
docker build -t vetclinic-backend:1.0.0 .

# Run
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/vetclinic_db \
  vetclinic-backend:1.0.0
```

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Con coverage
mvn test jacoco:report

# Tests de integración
mvn verify
```

## 📊 Monitoreo

### Actuator Endpoints

```
GET /actuator/health      # Estado de salud
GET /actuator/info        # Información de la app
GET /actuator/metrics     # Métricas
```

## 🔄 Datos Iniciales

Al iniciar la aplicación por primera vez, se crean automáticamente:

### Usuario Administrador
- **Username**: admin
- **Password**: admin123
- **Email**: admin@vetclinic.com
- **Rol**: ADMIN

### Roles
- ADMIN (con todos los permisos)
- VETERINARIAN
- RECEPTIONIST

### Permisos
- USER_READ, USER_WRITE, USER_DELETE
- PATIENT_READ, PATIENT_WRITE, PATIENT_DELETE
- APPOINTMENT_READ, APPOINTMENT_WRITE, APPOINTMENT_DELETE
- Y más...

## 📝 Logs

Los logs se guardan en:
- `logs/application.log` - Logs de aplicación
- `logs/error.log` - Solo errores

Configuración en `application.yml`:
```yaml
logging:
  level:
    com.vetclinic: DEBUG
    org.springframework.web: INFO
  file:
    name: logs/application.log
```

## 🚀 Deployment

### Variables de Entorno para Producción

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/vetclinic_db
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your-production-secret-key-min-64-chars
```

## 🤝 Contribución

### Para DEVs del equipo

1. **Clonar el repositorio**
2. **Checkout a develop**: `git checkout develop`
3. **Crear tu feature branch**: `git checkout -b feature/tu-modulo`
4. **Implementar tu módulo**
5. **Commit con convención**: `git commit -m "feat(tu-modulo): descripción"`
6. **Push**: `git push origin feature/tu-modulo`
7. **Crear Pull Request a develop**

### Estructura de un módulo completo

```
controller/
  - TuController.java
dto/
  - tu/
    - CreateTuRequest.java
    - UpdateTuRequest.java
    - TuDTO.java
entity/
  - TuEntity.java
repository/
  - TuRepository.java
service/
  - TuService.java
```

## 📚 Documentación Adicional

- [Postman Collection](../VetClinic-Pro-API.postman_collection.json)
- [Frontend Repository](https://github.com/Carlos1AB1/frontend-vetclinio)
- [Diagramas de Arquitectura](./docs/architecture.md)

## 🐛 Troubleshooting

### Error: "Unable to connect to database"
```bash
# Verificar que PostgreSQL esté corriendo
docker ps

# Ver logs de PostgreSQL
docker logs vetclinic-postgres
```

### Error: "JWT token expired"
```bash
# Usar el refresh token para obtener nuevo access token
POST /api/auth/refresh
{
  "refreshToken": "tu-refresh-token"
}
```

## 📞 Contacto

- **Equipo**: DEV 1
- **Proyecto**: VetClinic Pro
- **Versión**: 1.0.0

---

Desarrollado con ☕ usando Spring Boot 3
