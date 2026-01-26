package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.PresentationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String code;

    @NotBlank(message = "Active ingredient is required")
    private String activeIngredient;

    @NotBlank(message = "Concentration is required")
    private String concentration;

    @NotNull(message = "Presentation is required")
    private PresentationType presentation;

    private String indications;

    private String contraindications;

    private BigDecimal price;

    private Boolean active;

    private UUID pharmaceuticalId;

    private UUID distributorId;
}
