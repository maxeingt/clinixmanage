package gt.com.xfactory.service.impl;

import gt.com.xfactory.dto.request.*;
import gt.com.xfactory.dto.response.*;
import gt.com.xfactory.dto.response.DashboardWidgetsDto.*;
import gt.com.xfactory.dto.response.WidgetConfigDto.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.DashboardWidgetConfigEntity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import gt.com.xfactory.service.widget.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import jakarta.transaction.*;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class DashboardWidgetService {

    private static final List<WidgetType> ALL_WIDGETS = List.of(WidgetType.values());
    private static final int MAX_ACTIVE_WIDGETS = 3;

    private static final Map<String, List<WidgetItem>> DEFAULT_WIDGETS = Map.of(
            "doctor", List.of(
                    new WidgetItem(WidgetType.NEXT_APPOINTMENT, 1),
                    new WidgetItem(WidgetType.DELAYS, 2),
                    new WidgetItem(WidgetType.PENDING_LAB_ORDERS, 3)
            ),
            "secretary", List.of(
                    new WidgetItem(WidgetType.NEXT_APPOINTMENT, 1),
                    new WidgetItem(WidgetType.TODAY_CANCELLATIONS, 2),
                    new WidgetItem(WidgetType.DELAYS, 3)
            ),
            "admin", List.of(
                    new WidgetItem(WidgetType.WEEKLY_SUMMARY, 1),
                    new WidgetItem(WidgetType.TODAY_CANCELLATIONS, 2),
                    new WidgetItem(WidgetType.DELAYS, 3)
            )
    );

    @Inject
    DashboardWidgetConfigRepository configRepository;

    @Inject
    MedicalAppointmentRepository appointmentRepository;

    @Inject
    SecurityContextService securityContextService;

    @Inject
    Instance<WidgetResolver> widgetResolvers;

    private Map<WidgetType, WidgetResolver> resolverMap;

    @PostConstruct
    void init() {
        resolverMap = new HashMap<>();
        for (WidgetResolver resolver : widgetResolvers) {
            resolverMap.put(resolver.getType(), resolver);
        }
    }

    public WidgetConfigDto getWidgetConfig(UUID clinicId) {
        UUID userId = securityContextService.getCurrentUserId();
        Optional<DashboardWidgetConfigEntity> config = configRepository.findByUserAndClinic(userId, clinicId);

        boolean isDefault = config.isEmpty();
        List<WidgetItem> activeItems = config
                .map(DashboardWidgetConfigEntity::getWidgets)
                .orElseGet(this::getDefaultWidgets);

        List<WidgetItemDto> activeWidgets = activeItems.stream()
                .sorted(Comparator.comparingInt(WidgetItem::getOrder))
                .map(item -> WidgetItemDto.builder()
                        .type(item.getType())
                        .order(item.getOrder())
                        .build())
                .collect(Collectors.toList());

        return WidgetConfigDto.builder()
                .availableWidgets(ALL_WIDGETS)
                .activeWidgets(activeWidgets)
                .isDefault(isDefault)
                .build();
    }

    @Transactional
    public WidgetConfigDto saveWidgetConfig(WidgetConfigRequest request) {
        UUID userId = securityContextService.getCurrentUserId();

        if (request.getWidgets().size() > MAX_ACTIVE_WIDGETS) {
            throw new BadRequestException("Máximo " + MAX_ACTIVE_WIDGETS + " widgets activos");
        }

        long distinctTypes = request.getWidgets().stream()
                .map(WidgetConfigRequest.WidgetItemRequest::getType)
                .distinct()
                .count();
        if (distinctTypes != request.getWidgets().size()) {
            throw new BadRequestException("No se permiten widgets duplicados");
        }

        DashboardWidgetConfigId configId = new DashboardWidgetConfigId(userId, request.getClinicId());

        DashboardWidgetConfigEntity entity = configRepository.findByUserAndClinic(userId, request.getClinicId())
                .orElseGet(() -> {
                    DashboardWidgetConfigEntity newEntity = new DashboardWidgetConfigEntity();
                    newEntity.setId(configId);
                    return newEntity;
                });

        List<WidgetItem> widgets = request.getWidgets().stream()
                .map(item -> new WidgetItem(item.getType(), item.getOrder()))
                .collect(Collectors.toList());
        entity.setWidgets(widgets);

        configRepository.persist(entity);

        return getWidgetConfig(request.getClinicId());
    }

    public DashboardWidgetsDto getWidgets(UUID clinicId, UUID doctorId) {
        UUID currentDoctorId = securityContextService.getCurrentDoctorId();
        UUID effectiveDoctorId = currentDoctorId != null ? currentDoctorId : doctorId;

        UUID userId = securityContextService.getCurrentUserId();
        List<WidgetItem> activeItems = configRepository.findByUserAndClinic(userId, clinicId)
                .map(DashboardWidgetConfigEntity::getWidgets)
                .orElseGet(this::getDefaultWidgets);

        DayStatusDto dayStatus = buildDayStatus(clinicId, effectiveDoctorId);

        List<WidgetDataDto> widgetDataList = activeItems.stream()
                .sorted(Comparator.comparingInt(WidgetItem::getOrder))
                .map(item -> {
                    WidgetResolver resolver = resolverMap.get(item.getType());
                    Object data = resolver != null ? resolver.resolve(clinicId, effectiveDoctorId) : null;
                    return WidgetDataDto.builder()
                            .type(item.getType().name())
                            .order(item.getOrder())
                            .data(data)
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardWidgetsDto.builder()
                .dayStatus(dayStatus)
                .widgets(widgetDataList)
                .build();
    }

    private List<WidgetItem> getDefaultWidgets() {
        if (securityContextService.hasRole("admin")) {
            return DEFAULT_WIDGETS.get("admin");
        } else if (securityContextService.hasRole("secretary")) {
            return DEFAULT_WIDGETS.get("secretary");
        }
        return DEFAULT_WIDGETS.get("doctor");
    }

    private DayStatusDto buildDayStatus(UUID clinicId, UUID doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        StringBuilder where = new StringBuilder("1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (clinicId != null) {
            where.append(" AND m.clinic.id = :clinicId");
            params.put("clinicId", clinicId);
        }
        if (doctorId != null) {
            where.append(" AND m.doctor.id = :doctorId");
            params.put("doctorId", doctorId);
        }
        params.put("startOfDay", startOfDay);
        params.put("endOfDay", endOfDay);
        params.put("completed", AppointmentStatus.completed);
        params.put("cancelled", AppointmentStatus.cancelled);
        params.put("noShow", AppointmentStatus.no_show);
        params.put("pendingStatuses", List.of(
                AppointmentStatus.scheduled, AppointmentStatus.confirmed, AppointmentStatus.reopened));

        String jpql = "SELECT "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status = :completed THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status IN :pendingStatuses THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status = :cancelled THEN 1 ELSE 0 END), "
                + "SUM(CASE WHEN m.appointmentDate >= :startOfDay AND m.appointmentDate < :endOfDay AND m.status = :noShow THEN 1 ELSE 0 END) "
                + "FROM MedicalAppointmentEntity m WHERE " + where;

        var query = appointmentRepository.getEntityManager().createQuery(jpql, Object[].class);
        params.forEach(query::setParameter);
        Object[] row = query.getSingleResult();

        return DayStatusDto.builder()
                .todayAppointments(toLong(row[0]))
                .todayCompleted(toLong(row[1]))
                .todayPending(toLong(row[2]))
                .todayCancelled(toLong(row[3]))
                .todayNoShow(toLong(row[4]))
                .build();
    }

    private long toLong(Object value) {
        return value != null ? ((Number) value).longValue() : 0L;
    }
}
