package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto implements Serializable {
    private long todayAppointments;
    private long todayCompleted;
    private long todayPending;
    private long todayCancelled;
    private long todayNoShow;
    private long weeklyPatientsAttended;
    private long monthlyAppointments;
    private long monthlyCancellations;
}
