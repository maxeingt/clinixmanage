package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientSearchDto implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
    private int age;
    private String dpi;
    private String phone;
}
