package gt.com.xfactory.dto.response;

import gt.com.xfactory.entity.enums.PresentationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionMedicationDto implements Serializable {
    private UUID medicationId;
    private String medicationName;
    private String medicationCode;
    private String concentration;
    private PresentationType presentation;
    private String dose;
    private String frequency;
    private String duration;
    private Integer quantity;
    private String administrationRoute;
    private String specificIndications;
}
