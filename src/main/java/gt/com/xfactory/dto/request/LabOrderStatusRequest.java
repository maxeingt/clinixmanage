package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderStatusRequest {

    @NotNull(message = "Status is required")
    private LabOrderStatus status;
}
