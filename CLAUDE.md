# ClinicXManage - Backend

Sistema de gestión de clínicas médicas. Quarkus 3.19.4, Java 21, PostgreSQL, Hibernate Panache, Keycloak OIDC, OpenPDF, Lombok.

## Configuración

Puerto `8081`, context path `/clinicxmanage`, API base `/api/v1`. CORS para `http://localhost:4200`. Sin Flyway (migraciones manuales en `src/main/resources/db/migration/`).

## Estructura

```
src/main/java/gt/com/xfactory/
├── controller/       # REST (JAX-RS), v2/ para endpoints paginados nuevos
├── dto/request/      # *Request, *FilterDto (filter/)
├── dto/response/     # *Dto
├── entity/           # *Entity (enums/, converter/)
├── repository/       # PanacheRepository
├── service/impl/     # Lógica de negocio
└── utils/            # FilterBuilder, TenantContext, TenantRequestFilter
```

## Convenciones

**Nombrado**: `*Entity`, `*Dto`, `*Request`, `*FilterDto`, `*Repository`, `*Service`, `*Controller`.

**Imports**: Siempre wildcard (`import java.util.*;`), NO individuales. Excepción: imports que colisionan con otros paquetes (ej. `org.hibernate.annotations.TenantId`).

**Lombok** en todas las clases: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

**Patrones de clase**:
- Controladores: `@RequestScoped`, `@Path`, `@Produces(APPLICATION_JSON)`, `@RolesAllowed` a nivel clase
- Repositorios: `@ApplicationScoped implements PanacheRepository<Entity>`
- Servicios: `@ApplicationScoped @Slf4j`

**Paginación**: `@BeanParam CommonPageRequest` → `PageResponse<T>` via `PageResponse.toPageResponse()`.

**Logs**: Solo en operaciones de escritura importantes (`create`, `update` con resultado) y `warn` para fallbacks. NO loguear lecturas (`get`, `fetch`, `list`).

**Commits**: Español con prefijos `feat:`, `fix:`, `refactor:`, `perf:`, `docs:`.

## Seguridad

Roles: `super_admin`, `admin`, `doctor`, `secretary`. Un usuario tiene **un solo rol** (mutuamente excluyentes por diseño clínico). El campo `role` es `String` en `UserEntity`.

| Recurso | Super Admin | Admin | Doctor | Secretary |
|---------|-------------|-------|--------|-----------|
| Organizaciones | CRUD | — | — | — |
| Farmacia, Clínicas, Doctores, CIE-10 | — | CRUD | Ver | Ver |
| Especialidades | — | Ver | Ver | Ver |
| Pacientes, Citas | — | CRUD | CRUD | CRUD |
| Expedientes, Recetas, Lab, Diagnósticos | — | CRUD | CRUD | Ver |
| Usuarios | — | CRUD | Ver propio | Ver propio |

**Implementación**: `@RolesAllowed(todos)` en clase (lectura), override en método para escritura restringida.

**Privacidad de doctor**: El doctor solo ve en el **listado** los pacientes con los que tiene cita (subquery en `getPatients()`). El acceso por ID (`getPatientById`) no aplica este filtro — la seguridad real es el aislamiento por tenant.

## Multi-tenancy

- `@TenantId` en todas las entidades excepto `OrganizationEntity` (las orgs SON los tenants).
- `@TenantId` debe ser `String`, no `UUID`. Usar `columnDefinition = "uuid"` para que PostgreSQL maneje el cast.
- `TenantRequestFilter` extrae `organization_id` del JWT y lo setea en `TenantContext` al inicio de cada request.
- `CustomTenantResolver` lee de `TenantContext` (prioridad 1), luego RoutingContext, luego JWT manual decode como fallbacks.
- **Keycloak**: el client `angular-app` debe tener `organization_id` y `organization_slug` en los claims del token.

## Native Queries (bypasear @TenantId)

Usar native queries cuando se necesita acceso cross-tenant o cuando la entidad no tiene `@TenantId`:

```java
// Patrón: EntityManager via repository
em().createNativeQuery("SELECT ... FROM tabla WHERE ...", EntityClass.class)
    .setParameter("param", value)
    .getResultList();

// Para OrganizationService (org no tiene @TenantId):
private EntityManager em() { return organizationRepository.getEntityManager(); }
```

Casos donde se usa native query:
- `AuthService.syncCurrentUser()` — buscar usuario al login antes de resolver tenant
- `SecurityContextService.getCurrentUserId/getCurrentDoctorId()` — usuario del token actual
- `OrganizationService` — todas las operaciones (OrganizationEntity no tiene @TenantId)
- `UserService.createAdminForOrganization()` — validar org cross-tenant

## Notas Clave

1. **Roles excluyentes**: No existe multi-rol. Si admin necesita hacer algo de doctor, se ajusta la tabla de permisos.
2. **Relaciones doctor-clínica**: Soft delete con `active`, `unassignedAt`.
3. **Filtros JPQL**: Usar `FilterBuilder` con `addLike`, `addEquals`, `addCondition`, `addDateRange`.
4. **Transacciones**: `@Transactional` en servicios que modifican datos.
5. **PDF**: `PdfService` genera recetas y órdenes de laboratorio con OpenPDF.
6. **Keycloak clients**: `clinicxmanage-admin` (client_credentials, operaciones admin) y `quarkus-backend` (Direct Access Grants, validar credenciales).
7. **Many-to-Many**: Tablas intermedias con entidad propia (`DoctorClinicEntity`, `DoctorSpecialtyEntity`, `AppointmentDiagnosisEntity`, `PrescriptionMedicationEntity`).
8. **Detección duplicados**: `POST /patients` valida DPI único y nombre+birthdate antes de persistir, retorna 409 con `existingPatientId`.
9. **Búsqueda cross-doctor**: `GET /patients/search?q=` devuelve `PatientSearchDto` (datos no clínicos) sin filtro de appointments, máx 20 resultados.
10. **Lab orders**: Estado controlado via `PATCH /lab-orders/{id}/status` con `{"status": "in_progress"|"completed"|"cancelled"}`.
11. **Migraciones**: Siempre ejecutar el `.sql` manualmente en dev (`PGPASSWORD=... psql ...`). La tabla de auditoría `*_aud` también requiere los mismos cambios DDL.

## Dev

```bash
./mvnw quarkus:dev    # Desarrollo
./mvnw package        # Build
# Ejecutar migración manual:
PGPASSWORD=Manager1 psql -h localhost -p 5432 -U postgres -d postgres -f src/main/resources/db/migration/VXX__descripcion.sql
```
