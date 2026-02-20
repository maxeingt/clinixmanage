package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchDto implements Serializable {

    private List<PatientResult> patients;
    private List<AppointmentResult> appointments;
    private List<DoctorResult> doctors;
    private List<ClinicResult> clinics;
    private List<RecordResult> records;
    private List<MedicationResult> medications;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PatientResult implements Serializable {
        private UUID id;
        private String name;
        private String dpi;
        private int age;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AppointmentResult implements Serializable {
        private UUID id;
        private UUID patientId;
        private String patientName;
        private String doctorName;
        private LocalDateTime date;
        private String status;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DoctorResult implements Serializable {
        private UUID id;
        private String name;
        private String specialty;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClinicResult implements Serializable {
        private UUID id;
        private String name;
        private String address;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RecordResult implements Serializable {
        private UUID id;
        private UUID patientId;
        private UUID appointmentId;
        private String patientName;
        private LocalDateTime appointmentDate;
        private String diagnosis;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MedicationResult implements Serializable {
        private UUID id;
        private String name;
        private String code;
        private String concentration;
    }
}
