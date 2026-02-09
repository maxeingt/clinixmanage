# Plan de Auditoría y Mejora - ClinicXManage Backend

## Resumen Ejecutivo

Auditoría completa del backend de ClinicXManage. Se identificaron **42 hallazgos** organizados en 6 categorías: seguridad, rendimiento, código duplicado, malas prácticas, datos innecesarios y arquitectura. El plan de mejora está organizado en **5 fases** incrementales que no rompen funcionalidad existente.

---

## 1. PROBLEMAS DE SEGURIDAD (Prioridad: CRÍTICA)

### 1.1 Credenciales en texto plano en application.properties
**Archivo:** `src/main/resources/application.properties`
**Líneas:** 5, 17, 27
```properties
quarkus.datasource.password=Manager1
quarkus.oidc.credentials.secret=IsYn8VMCsJ85DQyES5Rd0yHunPkho7Yw
keycloak.admin.client-secret=pFdR50piFibomY1wU4xrIrc9JQAZvp7T
```
**Impacto:** Cualquier persona con acceso al repositorio tiene credenciales de DB y Keycloak.
**Solución:** Usar variables de entorno (`${DB_PASSWORD}`) o Quarkus config profiles con `application-prod.properties` no versionado. Mínimamente, agregar estos valores al `.gitignore` o usar `%dev.` profile prefix.

### 1.2 TLS verification deshabilitada
**Archivo:** `src/main/resources/application.properties:21`
```properties
quarkus.oidc.tls.verification=none
```
**Impacto:** Vulnerable a ataques Man-in-the-Middle en producción.
**Solución:** Solo usar `none` en perfil `%dev`, en producción usar `required`.

### 1.3 SQL habilitado en logs de producción
**Archivo:** `src/main/resources/application.properties:11`
```properties
quarkus.hibernate-orm.log.sql=true
```
**Impacto:** En producción expone estructura de queries y datos sensibles en logs.
**Solución:** Mover a `%dev.quarkus.hibernate-orm.log.sql=true`.

### 1.4 Falta validación de tamaño en CommonPageRequest
**Archivo:** `dto/request/CommonPageRequest.java`
**Impacto:** Un atacante puede solicitar `size=999999`, forzando carga masiva de datos en memoria.
**Solución:** Agregar `@Max(100)` o similar al campo `size`.

### 1.5 Inyección de campo de ordenamiento (Sort Injection)
**Archivo:** `utils/SortUtils.java`
**Impacto:** El campo de sort viene directo del usuario sin validación. Si se pasa un campo que no existe, puede exponer información de la estructura de la BD vía mensajes de error.
**Solución:** Validar que el campo de sort está en una lista blanca de campos permitidos por entidad.

### 1.6 Content-Disposition sin sanitización
**Archivo:** `controller/LabOrderController.java:128`
```java
.header("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"")
```
**Impacto:** Si el nombre de archivo contiene caracteres especiales, puede permitir HTTP header injection.
**Solución:** Sanitizar el filename antes de incluirlo en el header.

### 1.7 AuthController crea usuarios sin @Transactional completo
**Archivo:** `controller/AuthController.java:47`
**Impacto:** El método `me()` tiene `@Transactional` pero está en un **controller** (no service), y la sincronización de datos puede tener race conditions si dos requests llegan simultáneamente para el mismo usuario.
**Solución:** Mover lógica a un servicio dedicado `AuthService`.

### 1.8 HttpClient se crea nuevo en cada validación de password
**Archivo:** `service/impl/KeycloakAdminService.java:161`
```java
HttpClient httpClient = HttpClient.newHttpClient();
```
**Impacto:** Desperdicio de recursos, cada llamada crea un nuevo HTTP client con su pool de conexiones.
**Solución:** Crear el HttpClient como campo de instancia o reusar el existente.

---

## 2. PROBLEMAS DE RENDIMIENTO (Prioridad: ALTA)

### 2.1 N+1 Query en getDoctors (DoctorService)
**Archivo:** `service/impl/DoctorService.java:65-79`
```java
for (DoctorDto doctor : response.content) {
    List<SpecialtyDto> specialties = doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId());
    List<ClinicDto> clinics = doctorClinicRepository.findByDoctorId(doctor.getId())...
}
```
**Impacto:** Si la página tiene 25 doctores, se ejecutan 50 queries adicionales (25 por especialidades + 25 por clínicas).
**Solución:** Usar JOIN FETCH en una sola query o batch loading con `@BatchSize`.

### 2.2 N+1 Query en getDoctorsByClinic (ClinicService)
**Archivo:** `service/impl/ClinicService.java:118-121`
```java
for (DoctorDto doctor : response.content) {
    List<SpecialtyDto> specialties = doctorSpecialtyRepository.findSpecialtiesByDoctorId(doctor.getId());
}
```
**Impacto:** Mismo problema N+1 para especialidades por doctor.

### 2.3 N+1 Query en getDoctorsBySpecialtyId (SpecialtyService)
**Archivo:** `service/impl/SpecialtyService.java:55-62`
```java
return doctors.stream().map(entity -> {
    dto.setSpecialties(doctorSpecialtyRepository.findSpecialtiesByDoctorId(entity.getId()));
})
```

### 2.4 Dashboard ejecuta 9 queries separadas
**Archivo:** `service/impl/DashboardService.java:66-98`
**Impacto:** Cada llamada al dashboard ejecuta 9 queries al DB.
**Solución:** Consolidar en 1-2 queries con agregaciones condicionales (CASE WHEN) o una sola query nativa.

### 2.5 Filtrado de medical records por doctor en memoria (no en DB)
**Archivo:** `service/impl/MedicalRecordService.java:75-78`
```java
Stream<MedicalRecordEntity> stream = medicalRecordRepository.findByPatientId(patientId).stream();
if (currentDoctorId != null) {
    stream = stream.filter(r -> r.getDoctor().getId().equals(currentDoctorId));
}
```
**Impacto:** Carga TODOS los records del paciente y luego filtra en Java. Ineficiente con muchos registros.
**Solución:** Agregar filtro por doctorId directamente en la query del repositorio.

### 2.6 Mismo problema en prescriptions (MedicalRecordService)
**Archivo:** `service/impl/MedicalRecordService.java:227-231` y `240-245`
**Impacto:** Filtra prescripciones y prescripciones activas en memoria en vez de en DB.

### 2.7 Mismo problema en lab orders por paciente
**Archivo:** `service/impl/LabOrderService.java:135-139`
```java
.filter(order -> currentDoctorId == null || order.getDoctor().getId().equals(currentDoctorId))
```

### 2.8 Archivos almacenados como byte[] en la base de datos
**Archivo:** `entity/LabOrderAttachmentEntity.java:42-43`
```java
@Column(name = "file_data", nullable = false)
private byte[] fileData;
```
**Impacto:** Archivos de hasta 10MB almacenados directamente en PostgreSQL. Esto infla la DB, hace los backups más pesados y causa cargas de memoria innecesarias.
**Solución (largo plazo):** Migrar a almacenamiento en disco o S3, guardando solo la ruta en DB.

### 2.9 getAllClinics y getAllSpecialties sin paginación
**Archivo:** `service/impl/ClinicService.java:48-53`, `SpecialtyService.java:31-36`
**Impacto:** Si crecen los datos, estas queries cargan todo en memoria.
**Solución:** Si los catálogos se mantienen pequeños (<100 registros) es aceptable, pero documentar esta decisión.

### 2.10 findByIdOptional reimplementado innecesariamente
**Archivos:** `PatientRepository.java:13`, `DoctorRepository.java:13`, `UserRepository.java:13`, `MedicalAppointmentRepository.java:41`
**Impacto:** `PanacheRepository` ya provee `findByIdOptional()` de forma nativa.
**Solución:** Eliminar estos métodos duplicados.

---

## 3. CÓDIGO DUPLICADO (Prioridad: MEDIA)

### 3.1 getCurrentDoctorId() repetido en 4 servicios
**Archivos:**
- `PatientService.java:68-76`
- `MedicalRecordService.java:58-66`
- `LabOrderService.java:60-68`
- `DashboardService.java:36-44`

Son **exactamente el mismo código**.
**Solución:** Extraer a un servicio compartido `SecurityContextService` o `CurrentUserService`.

### 3.2 toDto de Doctor duplicado en 3 lugares
**Archivos:**
- `DoctorService.java:359-376` → `toDto`
- `SpecialtyService.java:72-87` → `toDoctorDto`
- Lógica inline en `DoctorClinicRepository.java:65-66`

**Solución:** Centralizar en un único mapper, preferiblemente en `DoctorService.toDto` y reusar.

### 3.3 Mapeo de ClinicDto desde DoctorClinic repetido 5 veces
**Archivos:** `DoctorService.java` líneas 69-77, 91-99, 160-168, 266-274, 347-355
```java
.map(dc -> ClinicDto.builder()
    .id(dc.getClinic().getId())
    .name(dc.getClinic().getName())
    .address(dc.getClinic().getAddress())
    .phone(dc.getClinic().getPhone())
    .build())
```
**Solución:** Extraer a un método `toClinicDto(DoctorClinicEntity)` o reusar el mapper de `ClinicService`.

### 3.4 Patrón de filter + query building duplicado
**Archivos:** Todos los servicios con paginación (PatientService, DoctorService, MedicationService, PharmaceuticalService, DistributorService, DiagnosisCatalogService, LabOrderService)
```java
StringBuilder query = new StringBuilder();
Map<String, Object> params = new HashMap<>();
List<String> conditions = new ArrayList<>();
// ... agregar condiciones ...
if (!conditions.isEmpty()) {
    query.append(String.join(" AND ", conditions));
}
```
**Solución:** Crear una clase `FilterBuilder` que encapsule este patrón.

### 3.5 Patrón CRUD soft-delete duplicado
**Archivos:** `PharmaceuticalService.java`, `DistributorService.java`, `MedicationService.java`
Estructura idéntica: list paginada, getById, create, update, soft-delete, getAllActive.
**Solución:** Si crece, considerar un `BaseCatalogService<E, D, R, F>` genérico.

### 3.6 NotificationDto.builder() duplicado en AppointmentSchedulerService
**Archivo:** `AppointmentSchedulerService.java`
La construcción del NotificationDto es muy similar en las 3 notificaciones (expirado, 30min, 10min).
**Solución:** Extraer método `buildNotification(type, appointment, message)`.

---

## 4. MALAS PRÁCTICAS (Prioridad: MEDIA)

### 4.1 Lógica de negocio en Controller (AuthController)
**Archivo:** `controller/AuthController.java`
**Impacto:** El controller tiene lógica de sincronización de usuarios, extracción de roles, creación de usuarios, y conversión a DTO. Viola la separación de responsabilidades.
**Solución:** Mover a un `AuthService`.

### 4.2 Repository accede a DTOs y Service (DoctorClinicRepository)
**Archivo:** `repository/DoctorClinicRepository.java`
**Impacto:** El repositorio importa `DoctorDto`, `PageResponse`, `DoctorService.toDto` y contiene lógica de paginación con CriteriaBuilder. Un repositorio no debería conocer DTOs ni servicios.
**Solución:** Mover la lógica de `findDoctorsByClinic` al servicio.

### 4.3 DoctorSpecialtyRepository retorna DTOs
**Archivo:** `repository/DoctorSpecialtyRepository.java:16-25`
```java
public List<SpecialtyDto> findSpecialtiesByDoctorId(UUID doctorId) {
```
**Impacto:** El repositorio no debería construir DTOs. Debería retornar entidades.
**Solución:** Retornar `List<DoctorSpecialtyEntity>` y mapear en el servicio.

### 4.4 Método createMedicalRecord con 12 parámetros
**Archivo:** `service/impl/MedicalRecordService.java:112-116`
```java
public MedicalRecordDto createMedicalRecord(UUID patientId, UUID appointmentId, UUID doctorId, UUID specialtyId,
    MedicalRecordType recordType, String chiefComplaint, String presentIllness,
    String physicalExam, String treatmentPlan, Map<String, Object> vitalSigns, Map<String, Object> specialtyData,
    Object attachments)
```
**Impacto:** Es muy propenso a errores y difícil de mantener. El controller ya desempaqueta el Request.
**Solución:** Pasar directamente `MedicalRecordRequest` al servicio.

### 4.5 Mismo problema en createPrescription (7 parámetros)
**Archivo:** `service/impl/MedicalRecordService.java:258-260`
**Solución:** Pasar `PrescriptionRequest` directamente.

### 4.6 Controller accede a Entity directamente (LabOrderController)
**Archivo:** `controller/LabOrderController.java:125`
```java
LabOrderAttachmentEntity attachment = labOrderService.getAttachmentEntity(attachmentId);
```
**Impacto:** El controller maneja entidades directamente, rompiendo la abstracción DTO.
**Solución:** Que el servicio retorne un DTO o un record con los datos necesarios.

### 4.7 Controller inyecta Repository directamente
**Archivos:**
- `UserController.java:38` → `UserRepository`
- `NotificationController.java:34` → `DoctorRepository`
- `AuthController.java:37` → `UserRepository`
**Impacto:** Los controllers no deberían acceder directamente a repositorios.
**Solución:** Mover lógica de validación de acceso a los servicios.

### 4.8 Inconsistencia de tipos temporales
**Archivo:** `entity/DoctorEntity.java`
```java
private LocalDate createdAt;  // DoctorEntity usa LocalDate
```
**Comparar con:** `entity/PatientEntity.java`
```java
private LocalDateTime createdAt;  // PatientEntity usa LocalDateTime
```
**Impacto:** Inconsistencia entre entidades. La mayoría usa `LocalDateTime` excepto `DoctorEntity`.
**Solución:** Unificar a `LocalDateTime` en todas las entidades.

### 4.9 Uso de `javax.json.bind` (ya deprecado)
**Archivo:** `entity/DoctorEntity.java:8`
```java
import javax.json.bind.annotation.JsonbTypeAdapter;
```
**Impacto:** Usa la API antigua de JSON-B (`javax.*`), mientras el proyecto usa `jakarta.*`.
**Solución:** Eliminar la anotación (el proyecto usa Jackson, no JSON-B).

### 4.10 Importación de jakarta.json.bind-api 1.0.2 innecesaria
**Archivo:** `pom.xml:79-82`
```xml
<dependency>
    <groupId>jakarta.json.bind</groupId>
    <artifactId>jakarta.json.bind-api</artifactId>
    <version>1.0.2</version>
</dependency>
```
**Impacto:** El proyecto usa Jackson (`quarkus-rest-jackson`), no JSON-B. Dependencia innecesaria.

### 4.11 Campos públicos en DTOs de filtro
**Archivos:** Todos los `*FilterDto.java`
```java
public UUID doctorId;
public String name;
```
**Impacto:** Viola encapsulamiento. Debería usar `@Data` de Lombok como los demás DTOs.

### 4.12 DoctorEntity no extiende PanacheEntityBase
**Archivo:** `entity/DoctorEntity.java:21`
```java
public class DoctorEntity implements Serializable {
```
**Impacto:** Inconsistencia - otras entidades como `PatientEntity`, `MedicalAppointmentEntity` etc. extienden `PanacheEntityBase`.

### 4.13 Falta manejo de excepciones global
**Impacto:** No hay un `ExceptionMapper` global. Las excepciones como `IllegalStateException`, `IllegalArgumentException` se propagan con stack traces.
**Solución:** Crear un `GlobalExceptionHandler` con `@Provider` que mapee excepciones comunes a respuestas HTTP apropiadas.

---

## 5. DATOS/DEPENDENCIAS INNECESARIOS (Prioridad: BAJA)

### 5.1 Dependencia postgresql con versión hardcodeada
**Archivo:** `pom.xml:54`
```xml
<version>42.6.0</version> <!-- revisa la última -->
```
**Impacto:** El BOM de Quarkus ya gestiona la versión del driver PostgreSQL. Versión 42.6.0 está desactualizada.
**Solución:** Eliminar la versión y dejar que el BOM la maneje.

### 5.2 Dependencia jakarta.json.bind-api
**Archivo:** `pom.xml:79-82`
**Impacto:** No se usa realmente. Solo se referencia en `DoctorEntity` con `@JsonbTypeAdapter`, que tampoco tiene efecto real porque se usa Jackson.

### 5.3 Imports no usados en PageResponse
**Archivo:** `dto/response/PageResponse.java:3-4`
```java
import com.fasterxml.jackson.databind.util.Converter;
```
**Impacto:** Import no utilizado.

### 5.4 Campo `calculateAge` no usado en PatientService
**Archivo:** `service/impl/PatientService.java:468-473`
```java
private int calculateAge(LocalDate birthdate) { ... }
```
**Impacto:** Método privado que nunca se invoca (la edad se calcula inline en `toDto`).

### 5.5 Dependencia quarkus-hibernate-orm redundante
**Archivo:** `pom.xml:63`
```xml
<artifactId>quarkus-hibernate-orm</artifactId>
```
**Impacto:** Ya se incluye transitivamente a través de `quarkus-hibernate-orm-panache`.

### 5.6 ErrorResponse DTO posiblemente no usado
**Archivo:** `dto/response/ErrorResponse.java`
**Impacto:** Si no hay `ExceptionMapper`, este DTO probablemente no se usa.

---

## 6. PROBLEMAS ARQUITECTURALES (Prioridad: MEDIA)

### 6.1 PatientService es un "God Service"
**Archivo:** `service/impl/PatientService.java` (598 líneas)
**Impacto:** Maneja pacientes, historial médico, citas médicas, diagnósticos, y transiciones de estado de citas. Demasiadas responsabilidades.
**Solución:** Extraer `MedicalAppointmentService` para manejar citas.

### 6.2 MedicalRecordService maneja demasiadas entidades
**Archivo:** `service/impl/MedicalRecordService.java` (397 líneas)
**Impacto:** Maneja expedientes médicos, templates de formularios, recetas, y medicamentos de recetas.
**Solución:** Extraer `PrescriptionService` para manejar recetas.

### 6.3 No hay tests unitarios ni de integración
**Directorio:** `src/test/` está vacío.
**Impacto:** No hay ninguna red de seguridad para refactors. Cualquier cambio puede romper funcionalidad sin ser detectado.
**Solución:** Crear tests básicos antes de iniciar cualquier refactoring.

### 6.4 No hay Exception Mappers
**Impacto:** Las excepciones como `IllegalStateException`, `IllegalArgumentException`, `ForbiddenException` retornan respuestas inconsistentes.
**Solución:** Crear `ExceptionMapper` para cada tipo de excepción esperada.

### 6.5 No hay interfaz de servicio
**Impacto:** Todos los servicios se inyectan por implementación concreta. Esto dificulta testing y mocking.
**Solución (opcional):** No es estrictamente necesario en Quarkus CDI, pero considerar para servicios clave.

---

## PLAN DE MEJORA POR FASES

### Fase 1: Seguridad y Configuración (1-2 días) - URGENTE
> **Objetivo:** Eliminar vulnerabilidades críticas sin tocar lógica de negocio.

| # | Tarea | Archivos | Riesgo |
|---|-------|----------|--------|
| 1.1 | Externalizar credenciales con variables de entorno | `application.properties` | Bajo |
| 1.2 | Mover config de dev a perfil `%dev` (TLS, SQL logs) | `application.properties` | Bajo |
| 1.3 | Agregar `@Max(100)` a `CommonPageRequest.size` | `CommonPageRequest.java` | Bajo |
| 1.4 | Sanitizar filename en Content-Disposition headers | `LabOrderController.java` | Bajo |
| 1.5 | Eliminar versión hardcodeada de PostgreSQL driver | `pom.xml` | Bajo |
| 1.6 | Eliminar dependencia `jakarta.json.bind-api` y `javax.json.bind` | `pom.xml`, `DoctorEntity.java` | Bajo |

### Fase 2: Eliminación de Código Duplicado (2-3 días)
> **Objetivo:** Consolidar código repetido sin cambiar comportamiento.

| # | Tarea | Archivos afectados | Riesgo |
|---|-------|--------------------|--------|
| 2.1 | Crear `SecurityContextService` con `getCurrentDoctorId()` | Nuevo + 4 servicios | Medio |
| 2.2 | Crear `FilterBuilder` para encapsular patrón de queries | Nuevo + 7 servicios | Medio |
| 2.3 | Centralizar `DoctorEntity -> DoctorDto` mapper | `DoctorService`, `SpecialtyService` | Bajo |
| 2.4 | Extraer `toClinicDto(DoctorClinicEntity)` | `DoctorService` | Bajo |
| 2.5 | Extraer `buildNotification()` en `AppointmentSchedulerService` | `AppointmentSchedulerService` | Bajo |
| 2.6 | Eliminar `findByIdOptional` redundantes | 4 repositorios | Bajo |
| 2.7 | Eliminar método `calculateAge` no usado | `PatientService.java` | Bajo |
| 2.8 | Eliminar import no usado en `PageResponse` | `PageResponse.java` | Bajo |

### Fase 3: Corrección de Malas Prácticas (3-4 días)
> **Objetivo:** Mejorar la separación de responsabilidades.

| # | Tarea | Archivos afectados | Riesgo |
|---|-------|--------------------|--------|
| 3.1 | Mover lógica de `AuthController` a `AuthService` | `AuthController`, nuevo `AuthService` | Medio |
| 3.2 | Mover lógica de paginación de `DoctorClinicRepository` a servicio | `DoctorClinicRepository`, `ClinicService` | Medio |
| 3.3 | Cambiar `DoctorSpecialtyRepository` para retornar entidades | `DoctorSpecialtyRepository`, servicios | Medio |
| 3.4 | Simplificar `createMedicalRecord` para recibir Request | `MedicalRecordService`, `MedicalRecordController` | Bajo |
| 3.5 | Simplificar `createPrescription` para recibir Request | `MedicalRecordService`, `MedicalRecordController` | Bajo |
| 3.6 | Eliminar acceso directo a Entity en `LabOrderController` | `LabOrderController`, `LabOrderService` | Bajo |
| 3.7 | Eliminar inyecciones de Repository en Controllers | 3 controllers, servicios | Medio |
| 3.8 | Unificar `LocalDate` → `LocalDateTime` en `DoctorEntity` | `DoctorEntity`, `DoctorService` | Bajo |
| 3.9 | Encapsular campos en Filter DTOs con `@Data` | 8 FilterDto files | Bajo |
| 3.10 | Crear `GlobalExceptionHandler` con `@Provider` | Nuevo archivo | Bajo |
| 3.11 | Hacer `DoctorEntity` extienda `PanacheEntityBase` | `DoctorEntity` | Bajo |

### Fase 4: Optimización de Rendimiento (3-5 días)
> **Objetivo:** Eliminar problemas N+1 y queries ineficientes.

| # | Tarea | Archivos afectados | Riesgo |
|---|-------|--------------------|--------|
| 4.1 | Resolver N+1 en `getDoctors` con JOIN FETCH o batch | `DoctorService`, `DoctorRepository` | Alto |
| 4.2 | Resolver N+1 en `getDoctorsByClinic` | `ClinicService`, repositorios | Alto |
| 4.3 | Resolver N+1 en `getDoctorsBySpecialtyId` | `SpecialtyService` | Alto |
| 4.4 | Mover filtrado de MedicalRecords por doctor a query DB | `MedicalRecordRepository`, `MedicalRecordService` | Medio |
| 4.5 | Mover filtrado de Prescriptions por doctor a query DB | `PrescriptionRepository`, `MedicalRecordService` | Medio |
| 4.6 | Mover filtrado de LabOrders por doctor a query DB | `LabOrderRepository`, `LabOrderService` | Medio |
| 4.7 | Consolidar queries del Dashboard | `DashboardService` | Medio |
| 4.8 | Agregar validación de campos de sort (whitelist) | `SortUtils`, servicios | Bajo |

### Fase 5: Mejoras Arquitecturales (5-7 días)
> **Objetivo:** Mejorar mantenibilidad a largo plazo.

| # | Tarea | Archivos afectados | Riesgo |
|---|-------|--------------------|--------|
| 5.1 | Crear tests básicos para endpoints existentes | Nuevos archivos en `src/test/` | Bajo |
| 5.2 | Extraer `MedicalAppointmentService` de `PatientService` | `PatientService`, nuevo servicio, controller | Alto |
| 5.3 | Extraer `PrescriptionService` de `MedicalRecordService` | `MedicalRecordService`, nuevo servicio | Alto |
| 5.4 | Evaluar migración de file storage a disco/S3 | `FileStorageService`, `LabOrderAttachmentEntity` | Alto |
| 5.5 | Eliminar dependencia `quarkus-hibernate-orm` redundante | `pom.xml` | Bajo |

---

## Matriz de Prioridad vs Esfuerzo

```
              BAJO ESFUERZO          ALTO ESFUERZO
            ┌─────────────────────┬─────────────────────┐
  ALTO      │ Fase 1 (Seguridad)  │ Fase 4 (Rendimiento)│
  IMPACTO   │ - Credenciales      │ - N+1 Queries       │
            │ - TLS/SQL logs      │ - Dashboard queries  │
            │ - Page size limit   │ - DB filtering       │
            ├─────────────────────┼─────────────────────┤
  BAJO      │ Fase 2 (Duplicados) │ Fase 5 (Arquitectura)│
  IMPACTO   │ - SecurityContext    │ - Extraer servicios  │
            │ - Mappers           │ - Tests              │
            │ Fase 3 (Prácticas)  │ - File storage       │
            │ - ExceptionHandler  │                      │
            └─────────────────────┴─────────────────────┘
```

## Recomendaciones Adicionales

1. **Antes de cualquier refactor:** Crear al menos tests de integración básicos (smoke tests) para los endpoints principales. Sin tests, cualquier refactor es un riesgo.
2. **Cada fase debe terminarse completamente** antes de pasar a la siguiente. No mezclar fases.
3. **Cada tarea de una fase = un commit separado.** Esto facilita rollback si algo falla.
4. **Fase 1 se puede hacer inmediatamente** sin riesgo de romper nada.
5. **Las fases 2 y 3 son seguras** si se verifica manualmente cada cambio.
6. **Las fases 4 y 5 requieren testing cuidadoso** porque cambian comportamiento de queries y estructura de servicios.
