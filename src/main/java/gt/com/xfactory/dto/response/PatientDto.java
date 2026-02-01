package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
    private int age;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;
    private String maritalStatus;
    private String occupation;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String allergies;
    private String chronicConditions;
    private String insuranceProvider;
    private String insuranceNumber;
    private String dpi;
    private String nationality;
    private BigDecimal height;
    private BigDecimal weight;
    private Boolean hasPathologicalHistory;
}
