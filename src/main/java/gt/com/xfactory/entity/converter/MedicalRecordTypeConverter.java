package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.MedicalRecordType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MedicalRecordTypeConverter implements AttributeConverter<MedicalRecordType, String> {

    @Override
    public String convertToDatabaseColumn(MedicalRecordType recordType) {
        if (recordType == null) {
            return null;
        }
        return recordType.name();
    }

    @Override
    public MedicalRecordType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return MedicalRecordType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return MedicalRecordType.consultation;
        }
    }
}
