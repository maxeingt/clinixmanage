package gt.com.xfactory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReopenAppointmentRequest {

    @NotNull(message = "Appointment date is required")
    private LocalDateTime appointmentDate;
}
