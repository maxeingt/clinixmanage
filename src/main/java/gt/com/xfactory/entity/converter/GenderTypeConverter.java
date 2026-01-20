package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.GenderType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderTypeConverter implements AttributeConverter<GenderType, String> {

    @Override
    public String convertToDatabaseColumn(GenderType genderType) {
        if (genderType == null) {
            return null;
        }
        return genderType.name();
    }

    @Override
    public GenderType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return GenderType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
