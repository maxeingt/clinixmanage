package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto implements Serializable {
    private UUID id;
    private String name;
    private Integer age;
    private String phone;
    private String address;
    private String maritalStatus;
    private String occupation;
}
