package gt.com.xfactory.service.widget;

import gt.com.xfactory.entity.enums.*;

import java.util.*;

public interface WidgetResolver {

    WidgetType getType();

    Object resolve(UUID clinicId, UUID doctorId);
}
