package gt.com.xfactory.entity;

import gt.com.xfactory.entity.enums.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;

import java.io.*;
import java.time.*;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "dashboard_widget_config")
@AllArgsConstructor
@NoArgsConstructor
public class DashboardWidgetConfigEntity extends PanacheEntityBase implements Serializable {

    @EmbeddedId
    private DashboardWidgetConfigId id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "widgets", columnDefinition = "jsonb", nullable = false)
    private List<WidgetItem> widgets = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @TenantId
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetItem implements Serializable {
        private WidgetType type;
        private int order;
    }
}
