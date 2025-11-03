# 🌿 Guía de Branches - VetClinic Pro Backend

## 📊 Estructura de Ramas Actual

### Ramas Principales
- **`main`** ✅ - Código en producción (solo configuración base)
- **`develop`** ✅ - Rama de integración para desarrollo

### Feature Branches (Módulos Implementados por DEV 1)

| Branch | Descripción | Estado | Archivos Principales |
|--------|-------------|--------|---------------------|
| `feature/core-entities` | Entidades JPA base | ✅ Subido | User, Role, Permission, PasswordResetToken + Repositories |
| `feature/security-jwt` | Seguridad JWT | ✅ Subido | JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig |
| `feature/auth-module` | Autenticación | ✅ Subido | AuthController, AuthService, DTOs de auth |
| `feature/user-management` | Gestión de usuarios | ✅ Subido | UserController, UserService, DTOs de user |
| `feature/dashboard` | Dashboard | ✅ Subido | DashboardController, DashboardService |
| `feature/core-config` | Configuraciones base | ✅ Subido | CORS, OpenAPI, DataInitializer, application.yml |
| `feature/exception-handling` | Manejo de excepciones | ✅ Subido | GlobalExceptionHandler, excepciones custom |
| `feature/design-patterns` | Patrones de diseño | ✅ Subido | 15+ patrones (Chain, Adapter, Strategy, etc.) |

---

## 🚀 Módulos Pendientes (Para otros DEVs)

### DEV 2 - Módulo de Pacientes
**Branch**: `feature/patient-management`

**Archivos a crear**:
```
controller/
  - PatientController.java
entity/
  - Patient.java
repository/
  - PatientRepository.java
service/
  - PatientService.java
dto/patient/
  - CreatePatientRequest.java
  - UpdatePatientRequest.java
  - PatientDTO.java
```

**Endpoints**:
- `GET /api/patients` - Listar pacientes
- `GET /api/patients/{id}` - Obtener paciente
- `POST /api/patients` - Crear paciente
- `PUT /api/patients/{id}` - Actualizar paciente
- `DELETE /api/patients/{id}` - Eliminar paciente

---

### DEV 3 - Módulo de Propietarios
**Branch**: `feature/owner-management`

**Archivos a crear**:
```
controller/
  - OwnerController.java
entity/
  - Owner.java
repository/
  - OwnerRepository.java
service/
  - OwnerService.java
dto/owner/
  - CreateOwnerRequest.java
  - UpdateOwnerRequest.java
  - OwnerDTO.java
```

---

### DEV 4 - Módulo de Citas
**Branch**: `feature/appointment-management`

**Archivos a crear**:
```
controller/
  - AppointmentController.java
entity/
  - Appointment.java
repository/
  - AppointmentRepository.java
service/
  - AppointmentService.java
dto/appointment/
  - CreateAppointmentRequest.java
  - UpdateAppointmentRequest.java
  - AppointmentDTO.java
```

---

### DEV 5 - Módulo de Historiales Médicos
**Branch**: `feature/medical-records`

**Archivos a crear**:
```
controller/
  - MedicalRecordController.java
entity/
  - MedicalRecord.java
repository/
  - MedicalRecordRepository.java
service/
  - MedicalRecordService.java
dto/medicalrecord/
  - CreateMedicalRecordRequest.java
  - UpdateMedicalRecordRequest.java
  - MedicalRecordDTO.java
```

---

### DEV 6 - Módulo de Inventario
**Branch**: `feature/inventory-management`

**Archivos a crear**:
```
controller/
  - InventoryController.java
entity/
  - InventoryItem.java
repository/
  - InventoryRepository.java
service/
  - InventoryService.java
dto/inventory/
  - CreateInventoryItemRequest.java
  - UpdateInventoryItemRequest.java
  - InventoryItemDTO.java
```

---

## 📝 Workflow de Git para el Equipo

### 1. Clonar el repositorio
```bash
git clone https://github.com/Carlos1AB1/backend-vetclinic.git
cd backend-vetclinic
```

### 2. Crear tu feature branch desde develop
```bash
git checkout develop
git pull origin develop
git checkout -b feature/tu-modulo
```

### 3. Trabajar en tu módulo
```bash
# Crear tus archivos
# ...

# Agregar archivos al staging
git add .

# Hacer commit con convención
git commit -m "feat(tu-modulo): descripción del cambio"
```

### 4. Subir tu branch
```bash
git push -u origin feature/tu-modulo
```

### 5. Crear Pull Request
- Ve a: https://github.com/Carlos1AB1/backend-vetclinic
- Crea un Pull Request de `feature/tu-modulo` → `develop`
- Solicita revisión del equipo
- Después de aprobación, hacer merge

### 6. Actualizar tu rama local
```bash
git checkout develop
git pull origin develop
```

---

## 📐 Convención de Commits

Usa el formato: `tipo(modulo): descripción`

**Tipos**:
- `feat` - Nueva funcionalidad
- `fix` - Corrección de bug
- `docs` - Documentación
- `refactor` - Refactorización
- `test` - Tests
- `chore` - Mantenimiento

**Ejemplos**:
```bash
git commit -m "feat(patient): Implementación de CRUD de pacientes"
git commit -m "fix(auth): Corregir validación de token expirado"
git commit -m "docs(readme): Actualizar guía de instalación"
git commit -m "refactor(user): Mejorar consulta de usuarios"
git commit -m "test(appointment): Agregar tests unitarios"
```

---

## 🔄 Flujo de Integración

```
feature/tu-modulo
       ↓
    [PR + Review]
       ↓
    develop (integración)
       ↓
    [Testing + QA]
       ↓
      main (producción)
```

---

## ✅ Checklist antes de hacer PR

- [ ] El código compila sin errores
- [ ] Seguiste la estructura del proyecto
- [ ] Usaste las convenciones de nombres
- [ ] Agregaste validaciones necesarias
- [ ] Los DTOs tienen anotaciones de validación
- [ ] Los endpoints tienen seguridad (`@PreAuthorize`)
- [ ] Documentaste el código (JavaDoc)
- [ ] El commit message sigue la convención
- [ ] Hiciste pull de develop antes de tu último commit

---

## 🏗️ Estructura de un Módulo Completo

```java
// 1. Entity (JPA)
@Entity
@Table(name = "tu_entidad")
public class TuEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // campos...
}

// 2. Repository
@Repository
public interface TuRepository extends JpaRepository<TuEntidad, Long> {
    // métodos custom si necesitas
}

// 3. DTOs
public class CreateTuRequest {
    @NotBlank
    private String campo;
}

public class TuDTO {
    private Long id;
    private String campo;
}

// 4. Service
@Service
@RequiredArgsConstructor
public class TuService {
    private final TuRepository repository;
    
    public TuDTO create(CreateTuRequest request) {
        // lógica
    }
}

// 5. Controller
@RestController
@RequestMapping("/api/tu-recurso")
@RequiredArgsConstructor
public class TuController {
    private final TuService service;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TuDTO> create(@Valid @RequestBody CreateTuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(request));
    }
}
```

---

## 🔐 Seguridad en Endpoints

```java
// Solo ADMIN
@PreAuthorize("hasRole('ADMIN')")

// ADMIN o VETERINARIAN
@PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN')")

// Cualquier usuario autenticado
@PreAuthorize("isAuthenticated()")

// Público (no poner anotación)
```

---

## 📚 Recursos Útiles

- **Frontend Repository**: https://github.com/Carlos1AB1/frontend-vetclinio
- **Postman Collection**: Disponible en el proyecto
- **Roles disponibles**: ADMIN, VETERINARIAN, RECEPTIONIST
- **Base de datos**: PostgreSQL (vetclinic_db)
- **Puerto backend**: 8081
- **Base URL**: http://localhost:8081/api

---

## 🤝 Comunicación del Equipo

- **Antes de empezar**: Avisa en el grupo qué módulo vas a implementar
- **Durante desarrollo**: Mantén tu branch actualizado con develop
- **Al terminar**: Crea PR y notifica para revisión
- **Conflictos**: Coordina con el equipo para resolverlos

---

## 🐛 Resolución de Problemas

### Error: "Already on 'develop'"
```bash
git pull origin develop
```

### Error: "Merge conflict"
```bash
git checkout develop
git pull
git checkout feature/tu-modulo
git merge develop
# Resolver conflictos
git add .
git commit -m "chore: resolver conflictos con develop"
```

### Error: "Push rejected"
```bash
git pull origin feature/tu-modulo --rebase
git push
```

---

## 📞 Contacto

- **Equipo**: DEV 1, 2, 3, 4, 5, 6
- **Repositorio**: https://github.com/Carlos1AB1/backend-vetclinic
- **Email**: cabaron_23@cue.edu.co

---

**¡Buena suerte con tu módulo! 🚀**
