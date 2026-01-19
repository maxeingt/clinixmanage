package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalHistGynecoObstetricDto implements Serializable {
    private UUID id;
    private UUID patientId;
    private String medicalHistoryType;
    private LocalDate lastMenstrualPeriod;
    private Double weight;
    private Double height;
    private Double duration;
    private Double cycles;
    private String reliable;
    private String papanicolaou;
}
