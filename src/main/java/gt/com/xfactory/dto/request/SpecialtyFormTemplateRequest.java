package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialtyFormTemplateRequest {

    @NotNull(message = "specialtyId es requerido")
    private UUID specialtyId;

    @NotBlank(message = "formName es requerido")
    @Size(max = 100, message = "formName no debe exceder 100 caracteres")
    private String formName;

    private String description;

    @NotNull(message = "formSchema es requerido")
    private Map<String, Object> formSchema;
}
