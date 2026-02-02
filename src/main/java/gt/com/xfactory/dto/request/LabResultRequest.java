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
    private String testName;

    private String testCode;

    private String value;

    private String unit;

    private BigDecimal referenceMin;

    private BigDecimal referenceMax;

    private Boolean isAbnormal;

    private LocalDateTime resultDate;
}
