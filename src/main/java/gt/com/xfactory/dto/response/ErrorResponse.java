package gt.com.xfactory.dto.response;

import java.time.*;

public class ErrorResponse {
    public int status;
    public String message;
    public LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public ErrorResponse(String message) {
        this.message = message;
    }
}
