package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultDto implements Serializable {
    private UUID id;
    private UUID labOrderId;
    private String testName;
    private String testCode;
    private String value;
    private String unit;
    private BigDecimal referenceMin;
    private BigDecimal referenceMax;
    private Boolean isAbnormal;
    private LocalDateTime resultDate;
}
