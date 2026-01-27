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

## Estructura del Proyecto

```
src/main/java/gt/com/xfactory/
├── controller/     # Controladores REST (JAX-RS)
├── dto/
│   ├── request/    # DTOs de entrada (*Request, *FilterDto)
│   └── response/   # DTOs de salida (*Dto)
├── entity/
│   ├── enums/      # Enums (AppointmentStatus, GenderType, BloodType, MedicalRecordType)
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
@RolesAllowed("user")  // Siempre proteger con autenticación
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
| `UserClinicPermissionEntity` | Permisos de usuario por clínica |

### Relaciones Many-to-Many
- `DoctorClinicEntity` - Doctor ↔ Clinic (con `active`, `assignedAt`, `unassignedAt`)
- `DoctorSpecialtyEntity` - Doctor ↔ Specialty

## Endpoints Principales

| Recurso | Path |
|---------|------|
| Doctores | `/api/v1/doctors` |
| Pacientes | `/api/v1/patients` |
| Clínicas | `/api/v1/clinics` |
| Especialidades | `/api/v1/specialties` |
| Expedientes | `/api/v1/medical-records` |
| Recetas PDF | `/api/v1/medical-records/prescriptions/{id}/pdf` |
| Usuarios | `/api/v1/users` |
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

## Notas Importantes

1. **Seguridad**: Todos los endpoints deben tener `@RolesAllowed("user")` a nivel de clase. NO usar `@PermitAll` excepto casos muy específicos.

2. **Relaciones doctor-clínica**: Usar soft delete con campo `active`. Al remover, poner `active=false` y `unassignedAt=now()`.

3. **DTOs de respuesta**: Incluir relaciones necesarias para el frontend (ej: `DoctorDto` incluye `specialties` y `clinics`).

4. **Filtros**: Usar `QueryUtils.addLikeCondition()` para búsquedas con LIKE.

5. **Transacciones**: Usar `@Transactional` en métodos de servicio que modifican datos.

6. **Generación de PDF**: Usar `PdfService` (`@ApplicationScoped`) para generar PDFs con OpenPDF. Actualmente soporta generación de recetas médicas. Para agregar nuevos tipos de PDF, añadir métodos en este servicio.
