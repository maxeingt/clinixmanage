package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisCatalogRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 500, message = "Name must not exceed 500 characters")
    private String name;

    @Size(max = 250, message = "Category must not exceed 250 characters")
    private String category;

    @Size(max = 250, message = "Chapter must not exceed 250 characters")
    private String chapter;
}
