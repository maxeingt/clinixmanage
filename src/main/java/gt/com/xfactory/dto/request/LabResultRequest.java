package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.*;
import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultRequest {

    @NotBlank(message = "Test name is required")
    @Size(max = 200, message = "Test name must not exceed 200 characters")
    private String testName;

    @Size(max = 50, message = "Test code must not exceed 50 characters")
    private String testCode;

    @Size(max = 255, message = "Value must not exceed 255 characters")
    private String value;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    private BigDecimal referenceMin;

    private BigDecimal referenceMax;

    private Boolean isAbnormal;

    private LocalDateTime resultDate;
}
