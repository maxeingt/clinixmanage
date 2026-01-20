package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.AppointmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AppointmentStatusConverter implements AttributeConverter<AppointmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AppointmentStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public AppointmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return AppointmentStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return AppointmentStatus.scheduled;
        }
    }
}
