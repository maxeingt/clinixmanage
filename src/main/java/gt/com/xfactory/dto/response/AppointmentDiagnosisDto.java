package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDiagnosisDto implements Serializable {
    private UUID id;
    private UUID diagnosisId;
    private String code;
    private String name;
    private String type;
    private String notes;
}
