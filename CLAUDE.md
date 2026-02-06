# ClinicXManage - Backend

Sistema de gestión de clínicas médicas. Quarkus 3.19.4, Java 21, PostgreSQL, Hibernate Panache, Keycloak OIDC, OpenPDF, Lombok.

## Configuración

Puerto `8081`, context path `/clinicxmanage`, API base `/api/v1`. CORS para `http://localhost:4200`. Sin Flyway (migraciones manuales).

## Estructura

```
src/main/java/gt/com/xfactory/
├── controller/     # REST (JAX-RS)
├── dto/request/    # *Request, *FilterDto
├── dto/response/   # *Dto
├── entity/         # *Entity (enums/ y converter/)
├── repository/     # PanacheRepository
├── service/impl/   # Lógica de negocio
└── utils/          # QueryUtils, SortUtils
```

## Convenciones

**Nombrado**: `*Entity`, `*Dto`, `*Request`, `*FilterDto`, `*Repository`, `*Service`, `*Controller`, `*Id` (compuestos).

**Imports**: Siempre wildcard (`import java.util.*;`), NO individuales.

**Lombok** en todas las clases: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

**Patrones de clase**:
- Controladores: `@RequestScoped`, `@Path`, `@Produces(APPLICATION_JSON)`, `@RolesAllowed` a nivel clase
- Repositorios: `@ApplicationScoped implements PanacheRepository<Entity>`
- Servicios: `@ApplicationScoped @Slf4j`

**Paginación**: `@BeanParam CommonPageRequest` → `PageResponse<T>` (content, totalElements, totalPages, page, size).

**Commits**: Español con prefijos `feat:`, `fix:`, `refactor:`, `docs:`. Ej: `feat: agregar CRUD de doctores`

## Seguridad

Roles: `admin`, `doctor`, `secretary`. Solo `@RolesAllowed`, sin permisos granulares por clínica.

| Recurso | Admin | Doctor | Secretary |
|---------|-------|--------|-----------|
| Farmacia, Clínicas, Doctores, CIE-10 | CRUD | Ver | Ver |
| Especialidades | Ver | Ver | Ver |
| Pacientes, Citas | CRUD | CRUD | CRUD |
| Expedientes, Recetas, Lab, Diagnósticos | CRUD | CRUD | Ver |
| Usuarios | CRUD | Ver propio | Ver propio |

Implementación: `@RolesAllowed(todos)` en clase (lectura), `@RolesAllowed("admin")` en método (escritura), `@RolesAllowed({"admin","doctor"})` para expedientes/recetas/lab.

## Notas Clave

1. **Relaciones doctor-clínica**: Soft delete con `active`, `unassignedAt`.
2. **DTOs**: Incluir relaciones necesarias para frontend.
3. **Filtros**: `QueryUtils.addLikeCondition()` para LIKE.
4. **Transacciones**: `@Transactional` en servicios que modifican datos.
5. **PDF**: `PdfService` genera recetas y órdenes de laboratorio con OpenPDF.
6. **Keycloak**: `clinicxmanage-admin` (client_credentials, operaciones admin) y `quarkus-backend` (Direct Access Grants, validar credenciales).
7. **Many-to-Many**: Tablas intermedias con entidad propia (`DoctorClinicEntity`, `DoctorSpecialtyEntity`, `AppointmentDiagnosisEntity`, `PrescriptionMedicationEntity`).

## Dev

```bash
./mvnw quarkus:dev    # Desarrollo
./mvnw package        # Build
```
