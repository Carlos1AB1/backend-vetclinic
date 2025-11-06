# VetClinic Pro - Backend API

Backend REST API para el Sistema de Gestión de Clínica Veterinaria desarrollado con Java Spring Boot.

## 🚀 Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** + JWT
- **Spring Data JPA** + Hibernate
- **PostgreSQL**
- **Maven**
- **Swagger/OpenAPI** para documentación
- **Lombok** para reducir boilerplate
- **MapStruct** para mapeo de objetos

## 📋 Requisitos Previos

- JDK 17 o superior
- Maven 3.8+
- PostgreSQL 14+
- (Opcional) Docker y Docker Compose

## 🔧 Configuración

### 1. Base de Datos

Crear base de datos PostgreSQL:

```sql
CREATE DATABASE vetclinic_db;
CREATE USER vetclinic_user WITH PASSWORD 'vetclinic_pass';
GRANT ALL PRIVILEGES ON DATABASE vetclinic_db TO vetclinic_user;
```

### 2. Configuración de Aplicación

Editar `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vetclinic_db
    username: vetclinic_user
    password: vetclinic_pass

jwt:
  secret: your-secret-key-here-min-256-bits
  
spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password
```

### 3. Variables de Entorno (Recomendado para Producción)

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/vetclinic_db
export DATABASE_USERNAME=vetclinic_user
export DATABASE_PASSWORD=vetclinic_pass
export JWT_SECRET=your-256-bit-secret
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export FRONTEND_URL=http://localhost:5173
```

## 🏃 Ejecutar la Aplicación

### Modo Desarrollo

```bash
cd backend
mvn spring-boot:run
```

### Compilar y Ejecutar

```bash
mvn clean package
java -jar target/vetclinic-backend-1.0.0.jar
```

### Con Docker

```bash
docker-compose up -d
```

## 📚 Documentación API

Una vez iniciada la aplicación, acceder a:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔐 Autenticación

La API utiliza JWT (JSON Web Tokens) para autenticación.

### Login

```bash
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### Usar Token

```bash
Authorization: Bearer <your-jwt-token>
```

## 👤 Usuario por Defecto

Al iniciar la aplicación por primera vez, se crea un usuario administrador:

- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@vetclinic.com`

⚠️ **IMPORTANTE**: Cambiar la contraseña después del primer login.

## 📁 Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/vetclinic/
│   │   │   ├── config/              # Configuraciones
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # Entidades JPA
│   │   │   ├── exception/           # Excepciones personalizadas
│   │   │   ├── patterns/            # Patrones de diseño
│   │   │   │   ├── adapter/         # Adapter Pattern
│   │   │   │   ├── chain/           # Chain of Responsibility
│   │   │   │   └── strategy/        # Strategy Pattern
│   │   │   ├── repository/          # Repositorios JPA
│   │   │   ├── security/            # Seguridad y JWT
│   │   │   ├── service/             # Lógica de negocio
│   │   │   └── VetClinicApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/                        # Tests
├── pom.xml
└── README.md
```

## 🎨 Patrones de Diseño Implementados

### 1. Singleton
- `JwtTokenProvider`: Instancia única para gestión de JWT

### 2. Chain of Responsibility
- `ValidationHandler`: Cadena de validaciones
- `RegistrationValidationChain`: Validación de registro

### 3. Strategy
- `NotificationStrategy`: Diferentes canales de notificación
- `EmailNotificationStrategy`, `SmsNotificationStrategy`

### 4. Adapter
- `EmailServiceAdapter`: Adaptador para servicios de email
- `SpringMailAdapter`: Implementación con Spring Mail

## 🔒 Seguridad Implementada

- ✅ Autenticación JWT
- ✅ Autorización basada en roles
- ✅ Control de permisos granular
- ✅ Encriptación de contraseñas (BCrypt)
- ✅ Protección contra intentos de login
- ✅ Bloqueo automático de cuentas
- ✅ Tokens de recuperación de contraseña
- ✅ CORS configurado

## 📊 Endpoints Principales

### Autenticación
- `POST /auth/login` - Iniciar sesión
- `POST /auth/register` - Registrar usuario
- `POST /auth/refresh-token` - Refrescar token
- `POST /auth/forgot-password` - Solicitar reset de contraseña
- `POST /auth/reset-password` - Resetear contraseña
- `POST /auth/change-password` - Cambiar contraseña
- `GET /auth/me` - Obtener usuario actual

### Usuarios (Admin)
- `GET /users` - Listar usuarios
- `GET /users/{id}` - Obtener usuario
- `PUT /users/{id}` - Actualizar usuario
- `DELETE /users/{id}` - Eliminar usuario
- `POST /users/{id}/unlock` - Desbloquear usuario

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Con cobertura
mvn test jacoco:report
```

## 🚀 Despliegue

### Producción

1. Configurar variables de entorno
2. Usar perfil de producción:

```bash
java -jar -Dspring.profiles.active=prod target/vetclinic-backend-1.0.0.jar
```

### Docker

```bash
docker build -t vetclinic-backend .
docker run -p 8080:8080 --env-file .env vetclinic-backend
```

## 📝 Logs

Los logs se guardan en:
- Consola (development)
- `logs/vetclinic.log` (producción)

## 🛠️ Desarrollo

### Agregar Nueva Funcionalidad

1. Crear entity en `entity/`
2. Crear repository en `repository/`
3. Crear DTOs en `dto/`
4. Implementar service en `service/`
5. Crear controller en `controller/`
6. Agregar tests

### Hot Reload

El proyecto incluye Spring DevTools para hot reload en desarrollo.

## 🐛 Troubleshooting

### Error de Conexión a BD
- Verificar que PostgreSQL esté corriendo
- Verificar credenciales en `application.yml`

### Error JWT Invalid
- Verificar que el secret tenga al menos 256 bits
- Verificar que el token no haya expirado

### Email no se envía
- Verificar configuración SMTP
- Para Gmail, usar App Password

## 📞 Soporte

Para problemas o preguntas:
- Email: support@vetclinic.com
- Issues: GitHub Issues

## 📄 Licencia

MIT License - Ver LICENSE file para más detalles

## 👨‍💻 Autor

VetClinic Pro Development Team

---

**Versión**: 1.0.0  
**Última Actualización**: Noviembre 2025
