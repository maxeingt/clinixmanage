package gt.com.xfactory.controller;

import gt.com.xfactory.dto.response.*;
import jakarta.validation.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import lombok.extern.slf4j.*;

import java.util.stream.*;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof NotFoundException) {
            return buildResponse(Response.Status.NOT_FOUND, exception.getMessage());
        }

        if (exception instanceof ForbiddenException) {
            return buildResponse(Response.Status.FORBIDDEN, exception.getMessage());
        }

        if (exception instanceof NotAuthorizedException) {
            return buildResponse(Response.Status.UNAUTHORIZED, "No autorizado");
        }

        if (exception instanceof BadRequestException) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }

        if (exception instanceof IllegalArgumentException) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }

        if (exception instanceof IllegalStateException) {
            return buildResponse(Response.Status.CONFLICT, exception.getMessage());
        }

        if (exception instanceof ConstraintViolationException cve) {
            String message = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return buildResponse(Response.Status.BAD_REQUEST, message);
        }

        log.error("Error no manejado: ", exception);
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private Response buildResponse(Response.Status status, String message) {
        return Response.status(status)
                .entity(new ErrorResponse(status.getStatusCode(), message))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
