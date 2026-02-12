package gt.com.xfactory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DashboardWidgetConfigId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "clinic_id", nullable = false)
    private UUID clinicId;
}
