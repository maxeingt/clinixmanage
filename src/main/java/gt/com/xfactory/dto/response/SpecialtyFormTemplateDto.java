package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialtyFormTemplateDto implements Serializable {
    private UUID id;
    private UUID specialtyId;
    private String specialtyName;
    private String formName;
    private String description;
    private Map<String, Object> formSchema;
    private Boolean isActive;
    private Integer version;
}
