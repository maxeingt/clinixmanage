package gt.com.xfactory.dto.request;

import javax.json.bind.adapter.JsonbAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter implements JsonbAdapter<LocalDate, String> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate adaptFromJson(String json) throws Exception {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(json, dateTimeFormatter);
            return dateTime.toLocalDate();
        } catch (Exception e) {
            return LocalDate.parse(json, dateFormatter);
        }
    }

    @Override
    public String adaptToJson(LocalDate obj) throws Exception {
        return obj.toString();
    }
}
