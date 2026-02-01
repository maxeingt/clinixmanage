package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.AppointmentSource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AppointmentSourceConverter implements AttributeConverter<AppointmentSource, String> {

    @Override
    public String convertToDatabaseColumn(AppointmentSource source) {
        if (source == null) {
            return null;
        }
        return source.name();
    }

    @Override
    public AppointmentSource convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return AppointmentSource.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return AppointmentSource.web;
        }
    }
}
