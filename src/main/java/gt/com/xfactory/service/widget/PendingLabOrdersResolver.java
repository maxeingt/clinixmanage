package gt.com.xfactory.service.widget;

import gt.com.xfactory.dto.response.DashboardWidgetsDto.*;
import gt.com.xfactory.entity.*;
import gt.com.xfactory.entity.enums.*;
import gt.com.xfactory.repository.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import lombok.extern.slf4j.*;

import java.time.format.*;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class PendingLabOrdersResolver implements WidgetResolver {

    @Inject
    LabOrderRepository labOrderRepository;

    @Override
    public WidgetType getType() {
        return WidgetType.PENDING_LAB_ORDERS;
    }

    @Override
    public Object resolve(UUID clinicId, UUID doctorId) {
        StringBuilder where = new StringBuilder("l.status = :status");
        Map<String, Object> params = new HashMap<>();
        params.put("status", LabOrderStatus.pending);

        if (doctorId != null) {
            where.append(" AND l.doctor.id = :doctorId");
            params.put("doctorId", doctorId);
        }

        String jpql = "SELECT l FROM LabOrderEntity l "
                + "LEFT JOIN FETCH l.patient "
                + "LEFT JOIN FETCH l.results "
                + "WHERE " + where
                + " ORDER BY l.orderDate DESC";

        var query = labOrderRepository.getEntityManager().createQuery(jpql, LabOrderEntity.class);
        params.forEach(query::setParameter);
        query.setMaxResults(5);

        List<PendingLabItem> items = query.getResultList().stream()
                .map(lab -> PendingLabItem.builder()
                        .labOrderId(lab.getId())
                        .patientName(lab.getPatient().getFirstName() + " " + lab.getPatient().getLastName())
                        .orderDate(lab.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .pendingResults(lab.getResults() != null ? (int) lab.getResults().stream()
                                .filter(r -> r.getValue() == null)
                                .count() : 0)
                        .build())
                .collect(Collectors.toList());

        return PendingLabOrdersData.builder()
                .count(items.size())
                .items(items)
                .build();
    }
}
