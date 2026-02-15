package gt.com.xfactory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;
import org.hibernate.envers.*;

import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Getter
@Setter
@ToString
@Audited
@Entity
@Table(name = "lab_result")
@AllArgsConstructor
@NoArgsConstructor
public class LabResultEntity extends PanacheEntityBase implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_order_id", nullable = false)
    private LabOrderEntity labOrder;

    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @Column(name = "test_code", length = 50)
    private String testCode;

    @Column(name = "value", length = 255)
    private String value;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "reference_min", precision = 10, scale = 4)
    private BigDecimal referenceMin;

    @Column(name = "reference_max", precision = 10, scale = 4)
    private BigDecimal referenceMax;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @TenantId
    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private String organizationId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
