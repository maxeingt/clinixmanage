package gt.com.xfactory.dto.response;

import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardWidgetsDto implements Serializable {

    private DayStatusDto dayStatus;
    private List<WidgetDataDto> widgets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayStatusDto implements Serializable {
        private long todayAppointments;
        private long todayCompleted;
        private long todayPending;
        private long todayCancelled;
        private long todayNoShow;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WidgetDataDto implements Serializable {
        private String type;
        private int order;
        private Object data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NextAppointmentData implements Serializable {
        private UUID appointmentId;
        private String patientName;
        private String time;
        private String specialtyName;
        private String reason;
        private long minutesUntil;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DelayItem implements Serializable {
        private UUID appointmentId;
        private String patientName;
        private String scheduledTime;
        private long delayMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DelaysData implements Serializable {
        private int count;
        private List<DelayItem> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancellationItem implements Serializable {
        private UUID appointmentId;
        private String patientName;
        private String scheduledTime;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TodayCancellationsData implements Serializable {
        private int count;
        private List<CancellationItem> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklySummaryData implements Serializable {
        private long totalAppointments;
        private long completed;
        private long cancelled;
        private long noShow;
        private long patientsAttended;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingLabItem implements Serializable {
        private UUID labOrderId;
        private String patientName;
        private String orderDate;
        private int pendingResults;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingLabOrdersData implements Serializable {
        private int count;
        private List<PendingLabItem> items;
    }
}
