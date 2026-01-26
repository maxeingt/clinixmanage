package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.PresentationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PresentationTypeConverter implements AttributeConverter<PresentationType, String> {

    @Override
    public String convertToDatabaseColumn(PresentationType presentationType) {
        if (presentationType == null) {
            return null;
        }
        return presentationType.name();
    }

    @Override
    public PresentationType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return PresentationType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
