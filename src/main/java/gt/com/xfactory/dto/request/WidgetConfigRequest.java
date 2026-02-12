package gt.com.xfactory.dto.request;

import gt.com.xfactory.entity.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetConfigRequest {

    @NotNull(message = "clinicId es requerido")
    private UUID clinicId;

    @NotNull(message = "widgets es requerido")
    @Size(max = 3, message = "Máximo 3 widgets activos")
    private List<WidgetItemRequest> widgets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetItemRequest {
        @NotNull(message = "type es requerido")
        private WidgetType type;

        @Min(value = 1, message = "order mínimo es 1")
        @Max(value = 3, message = "order máximo es 3")
        private int order;
    }
}
