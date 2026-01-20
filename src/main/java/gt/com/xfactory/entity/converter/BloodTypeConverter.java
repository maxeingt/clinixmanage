package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.BloodType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BloodTypeConverter implements AttributeConverter<BloodType, String> {

    @Override
    public String convertToDatabaseColumn(BloodType bloodType) {
        if (bloodType == null) {
            return null;
        }
        return bloodType.getValue();
    }

    @Override
    public BloodType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return BloodType.fromValue(dbData);
    }
}
