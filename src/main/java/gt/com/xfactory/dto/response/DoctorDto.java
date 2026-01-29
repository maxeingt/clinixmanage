package gt.com.xfactory.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDto implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
    private int age;
    private String phone;
    private String address;
    private String mail;
    private UUID userId;
    private List<SpecialtyDto> specialties;
    private List<ClinicDto> clinics;
}
