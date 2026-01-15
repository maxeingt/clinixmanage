package gt.com.xfactory.dto.response;

import java.time.LocalDateTime;

public class ErrorResponse {
    public String message;
    public LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String message) {
        this.message = message;
    }
}
