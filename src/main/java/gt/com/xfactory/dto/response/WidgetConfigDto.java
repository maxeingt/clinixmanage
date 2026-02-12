package gt.com.xfactory.dto.response;

import gt.com.xfactory.entity.enums.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetConfigDto implements Serializable {

    private List<WidgetType> availableWidgets;
    private List<WidgetItemDto> activeWidgets;
    private boolean isDefault;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WidgetItemDto implements Serializable {
        private WidgetType type;
        private int order;
    }
}
