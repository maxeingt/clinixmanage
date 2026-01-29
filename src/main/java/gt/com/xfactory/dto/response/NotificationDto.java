package gt.com.xfactory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String type;
    private UUID appointmentId;
    private String patientName;
    private LocalDateTime appointmentDate;
    private String message;
    private LocalDateTime timestamp;
}
