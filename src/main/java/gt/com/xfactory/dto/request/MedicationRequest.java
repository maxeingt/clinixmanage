package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Active ingredient is required")
    @Size(max = 200, message = "Active ingredient must not exceed 200 characters")
    private String activeIngredient;

    @NotBlank(message = "Concentration is required")
    @Size(max = 100, message = "Concentration must not exceed 100 characters")
    private String concentration;

    @NotNull(message = "Presentation is required")
    private PresentationType presentation;

    @Size(max = 5000, message = "Indications must not exceed 5000 characters")
    private String indications;

    @Size(max = 5000, message = "Contraindications must not exceed 5000 characters")
    private String contraindications;

    private BigDecimal price;

    private Boolean active;

    private UUID pharmaceuticalId;

    private UUID distributorId;
}
