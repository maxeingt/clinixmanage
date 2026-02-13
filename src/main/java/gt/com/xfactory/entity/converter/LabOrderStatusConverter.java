package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.LabOrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LabOrderStatusConverter implements AttributeConverter<LabOrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(LabOrderStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public LabOrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return LabOrderStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return LabOrderStatus.pending;
        }
    }
}
