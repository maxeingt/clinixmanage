# ClinicXManage - Backend

Sistema de gestión de clínicas médicas.

## Stack Tecnológico

- **Framework**: Quarkus 3.19.4
- **Java**: 21
- **Base de datos**: PostgreSQL
- **ORM**: Hibernate ORM con Panache
- **Seguridad**: Keycloak OIDC
- **Serialización**: Jackson
- **Validación**: Hibernate Validator
- **PDF**: OpenPDF (LibrePDF)
- **Utilidades**: Lombok, Apache Commons Lang3

## Configuración

- **Puerto**: 8081
- **Context path**: `/clinicxmanage`
- **API base**: `/api/v1`
- **CORS**: habilitado para `http://localhost:4200` (Angular frontend)
- **Migraciones**: No se usa Flyway. Los cambios de BD se aplican manualmente.

## Estructura del Proyecto

```
src/main/java/gt/com/xfactory/
├── controller/     # Controladores REST (JAX-RS)
├── dto/
│   ├── request/    # DTOs de entrada (*Request, *FilterDto)
│   └── response/   # DTOs de salida (*Dto)
├── entity/
│   ├── enums/      # Enums (AppointmentStatus, GenderType, BloodType, MedicalRecordType, LabOrderStatus, DiagnosisType, AppointmentSource, PresentationType)
│   └── converter/  # JPA Converters para enums
├── repository/     # Repositorios Panache
├── service/impl/   # Servicios de negocio
└── utils/          # Utilidades (QueryUtils, SortUtils)
```

## Convenciones de Código

### Nombrado
- **Entidades**: `*Entity` (ej: `DoctorEntity`, `PatientEntity`)
- **DTOs response**: `*Dto` (ej: `DoctorDto`, `ClinicDto`)
- **DTOs request**: `*Request` (ej: `DoctorRequest`, `PatientRequest`)
- **Filtros**: `*FilterDto` (ej: `DoctorFilterDto`)
- **Repositorios**: `*Repository` (ej: `DoctorRepository`)
- **Servicios**: `*Service` (ej: `DoctorService`)
- **Controladores**: `*Controller` (ej: `DoctorController`)
- **IDs compuestos**: `*Id` (ej: `DoctorClinicId`, `DoctorSpecialtyId`)

### Imports
- Usar **wildcard imports** (`import java.util.*;`), NO imports de línea individual.

### Lombok
Usar en todas las clases:
- `@Data` - getters, setters, equals, hashCode, toString
- `@Builder` - patrón builder
- `@NoArgsConstructor`, `@AllArgsConstructor` - constructores

### Controladores REST
```java
@RequestScoped
@Path("/api/v1/recurso")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "doctor", "secretary"})  // Roles permitidos a nivel de clase
public class RecursoController { }
```

### Repositorios
```java
@ApplicationScoped
public class RecursoRepository implements PanacheRepository<RecursoEntity> { }
```

### Servicios
```java
@ApplicationScoped
@Slf4j
public class RecursoService { }
```

## Modelo de Seguridad

La seguridad se maneja exclusivamente por **rol global** via `@RolesAllowed`. No existen permisos granulares por clínica.

### Roles: `admin`, `doctor`, `secretary`

### Permisos por recurso

| Recurso | Admin | Doctor | Secretary |
|---------|-------|--------|-----------|
| Farmacia (medicamentos, farmacéuticas, distribuidores) | CRUD | Solo ver | Solo ver |
| Clínicas | CRUD | Solo ver | Solo ver |
| Doctores | CRUD | Solo ver | Solo ver |
| Especialidades | Solo ver | Solo ver | Solo ver |
| Pacientes | CRUD | CRUD | CRUD |
| Citas | CRUD | CRUD | CRUD |
| Expedientes y recetas | CRUD | CRUD | Solo ver |
| Órdenes de laboratorio | CRUD | CRUD | Solo ver |
| Catálogo CIE-10 | CRUD | Solo ver | Solo ver |
| Diagnósticos de cita | CRUD | CRUD | Solo ver |
| Usuarios | CRUD | Solo ver propio | Solo ver propio |
| Cambio de password | Sí | Sí | Sí |
| Dashboard | Ver | Ver | Ver |

### Implementación
- `@RolesAllowed({"admin", "doctor", "secretary"})` a nivel de **clase** para permitir lectura a todos.
- `@RolesAllowed("admin")` a nivel de **método** para restringir escritura (POST/PUT/DELETE).
- `@RolesAllowed({"admin", "doctor"})` a nivel de **método** para expedientes/recetas.

## Entidades Principales

| Entidad | Descripción |
|---------|-------------|
| `DoctorEntity` | Médicos |
| `PatientEntity` | Pacientes |
| `ClinicEntity` | Clínicas |
| `SpecialtyEntity` | Especialidades médicas |
| `MedicalAppointmentEntity` | Citas médicas |
| `MedicalRecordEntity` | Expedientes médicos |
| `PrescriptionEntity` | Recetas |
| `UserEntity` | Usuarios del sistema |
| `LabOrderEntity` | Órdenes de laboratorio |
| `LabResultEntity` | Resultados de laboratorio |
| `DiagnosisCatalogEntity` | Catálogo CIE-10/ICD de diagnósticos |
| `AppointmentDiagnosisEntity` | Diagnósticos estructurados por cita |

### Relaciones Many-to-Many
- `DoctorClinicEntity` - Doctor ↔ Clinic (con `active`, `assignedAt`, `unassignedAt`)
- `DoctorSpecialtyEntity` - Doctor ↔ Specialty
- `AppointmentDiagnosisEntity` - Appointment ↔ DiagnosisCatalog (con `diagnosisType`: principal/secundario)
- `PrescriptionMedicationEntity` - Prescription ↔ Medication

## Endpoints Principales

| Recurso | Path |
|---------|------|
| Doctores | `/api/v1/doctors` |
| Pacientes | `/api/v1/patients` |
| Clínicas | `/api/v1/clinics` |
| Especialidades | `/api/v1/specialties` |
| Expedientes | `/api/v1/medical-records` |
| Recetas PDF | `/api/v1/medical-records/prescriptions/{id}/pdf` |
| Órdenes de laboratorio | `/api/v1/lab-orders` |
| Resultados de laboratorio | `/api/v1/lab-orders/{orderId}/results` |
| Catálogo CIE-10 | `/api/v1/diagnosis-catalog` |
| Usuarios | `/api/v1/users` |
| Cambio de password | `PUT /api/v1/users/{id}/change-password` |
| Medicamentos | `/api/v1/medications` |
| Farmacéuticas | `/api/v1/pharmaceuticals` |
| Distribuidores | `/api/v1/distributors` |
| Notificaciones | `/api/v1/notifications` |
| Dashboard | `/api/v1/dashboard` |
| Admin | `/api/v1/admin` |

## Paginación

Usar `CommonPageRequest` con `@BeanParam`:
```java
public PageResponse<Dto> listar(@BeanParam CommonPageRequest pageRequest) { }
```

Response: `PageResponse<T>` con campos `content`, `totalElements`, `totalPages`, `page`, `size`.

## Commits

Usar español con prefijos:
- `feat:` - nueva funcionalidad
- `fix:` - corrección de bug
- `bug:` - corrección de bug (alternativo)
- `refactor:` - refactorización
- `docs:` - documentación

Ejemplo: `feat: agregar CRUD de doctores`

## Comandos Útiles

```bash
# Desarrollo
./mvnw quarkus:dev

# Build
./mvnw package

# Build nativo
./mvnw package -Pnative
```

## Enums

| Enum | Valores |
|------|---------|
| `AppointmentStatus` | `scheduled`, `confirmed`, `in_progress`, `completed`, `cancelled`, `no_show`, `expired`, `reopened` |
| `GenderType` | `male`, `female`, `other`, `prefer_not_to_say` |
| `BloodType` | `A_POSITIVE`, `A_NEGATIVE`, `B_POSITIVE`, `B_NEGATIVE`, `AB_POSITIVE`, `AB_NEGATIVE`, `O_POSITIVE`, `O_NEGATIVE`, `UNKNOWN` |
| `MedicalRecordType` | `consultation`, `exam`, `procedure`, `lab_result`, `imaging`, `prescription`, `follow_up`, `referral` |
| `LabOrderStatus` | `pending`, `in_progress`, `completed`, `cancelled` |
| `DiagnosisType` | `principal`, `secundario` |
| `AppointmentSource` | `web`, `phone`, `walk_in` |

## Funcionalidades Recientes

### Módulo de Laboratorio
- CRUD de órdenes de laboratorio (`LabOrderEntity`) con resultados individuales (`LabResultEntity`).
- Una orden puede tener múltiples resultados. Eliminar una orden elimina sus resultados (cascade).
- Soporta valores de referencia (`referenceMin`, `referenceMax`) y flag de resultado anormal (`isAbnormal`).
- Filtros por paciente, doctor, estado y rango de fechas.

### Catálogo CIE-10 y Diagnósticos Estructurados
- `DiagnosisCatalogEntity`: catálogo de códigos CIE-10/ICD con código, nombre, categoría y capítulo.
- `AppointmentDiagnosisEntity`: vincula diagnósticos del catálogo a citas médicas con tipo (`principal`/`secundario`).
- Los diagnósticos se gestionan como parte de las citas médicas.

### Citas de Seguimiento
- `MedicalAppointmentEntity` tiene campo `followUpAppointment` (self-referencing FK).
- Permite vincular citas de seguimiento a citas originales.

### Reapertura de Citas
- Estado `reopened` en `AppointmentStatus` para reabrir citas expiradas.
- Se usa `ReopenAppointmentRequest` con nueva fecha de cita.

### Origen de Citas
- `AppointmentSource` (`web`, `phone`, `walk_in`) para rastrear cómo se creó la cita.

## Notas Importantes

1. **Seguridad**: Usar `@RolesAllowed` a nivel de clase y método según la matriz de permisos. NO usar `@PermitAll` excepto casos muy específicos.

2. **Relaciones doctor-clínica**: Usar soft delete con campo `active`. Al remover, poner `active=false` y `unassignedAt=now()`.

3. **DTOs de respuesta**: Incluir relaciones necesarias para el frontend (ej: `DoctorDto` incluye `specialties` y `clinics`).

4. **Filtros**: Usar `QueryUtils.addLikeCondition()` para búsquedas con LIKE.

5. **Transacciones**: Usar `@Transactional` en métodos de servicio que modifican datos.

6. **Generación de PDF**: Usar `PdfService` (`@ApplicationScoped`) para generar PDFs con OpenPDF. Actualmente soporta generación de recetas médicas. Para agregar nuevos tipos de PDF, añadir métodos en este servicio.

7. **Keycloak**: Dos clients configurados:
   - `clinicxmanage-admin` — client confidential con `client_credentials`, para operaciones admin (crear usuarios, resetear passwords).
   - `quarkus-backend` — client confidential con `Direct Access Grants` habilitado, usado para validar credenciales de usuario (ej: cambio de password).
