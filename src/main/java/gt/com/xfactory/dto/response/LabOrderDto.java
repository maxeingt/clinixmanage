package gt.com.xfactory.dto.response;

import gt.com.xfactory.entity.enums.*;
import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderDto implements Serializable {
    private UUID id;
    private UUID appointmentId;
    private UUID patientId;
    private String patientName;
    private UUID doctorId;
    private String doctorName;
    private LocalDateTime orderDate;
    private LabOrderStatus status;
    private String notes;
    private List<LabResultDto> results;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
