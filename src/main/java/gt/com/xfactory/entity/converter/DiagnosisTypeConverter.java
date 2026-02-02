package gt.com.xfactory.entity.converter;

import gt.com.xfactory.entity.enums.DiagnosisType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DiagnosisTypeConverter implements AttributeConverter<DiagnosisType, String> {

    @Override
    public String convertToDatabaseColumn(DiagnosisType diagnosisType) {
        if (diagnosisType == null) {
            return null;
        }
        return diagnosisType.name();
    }

    @Override
    public DiagnosisType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return DiagnosisType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
