package gt.com.xfactory.dto.response;

import gt.com.xfactory.entity.enums.PresentationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationDto implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private String code;
    private String activeIngredient;
    private String concentration;
    private PresentationType presentation;
    private String presentationDisplay;
    private String indications;
    private String contraindications;
    private BigDecimal price;
    private Boolean active;
    private UUID pharmaceuticalId;
    private String pharmaceuticalName;
    private UUID distributorId;
    private String distributorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
