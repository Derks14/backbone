package backbone.configs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.View;

import java.net.URI;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//
////    private final View error;
//
//    public GlobalExceptionHandler(View error) {
//        this.error = error;
//    }

    // use this base for all exceptions
    private ProblemDetail base(
            HttpStatus status,
            String title,
           Exception  exception,
            HttpServletRequest request
    ) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(exception.getMessage());
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        log.info("[{}] Handled {}  status={} path={} message={} ",
                request.getSession().getId(), title, status, request.getRequestURI(), exception.getMessage());

        return problemDetail;
    }


    @ExceptionHandler(BackboneException.class)
    public ResponseEntity<ProblemDetail> handleBackboneExceptions(BackboneException exception, HttpServletRequest request){
        String name = exception.getClass().getSimpleName();
        String sessionId = request.getSession().getId();

        // create problem for the error
        ProblemDetail problemDetail = this.base(exception.getHttpStatus(), name, exception, request);

        problemDetail.setProperty("code", exception.getCode());
        log.info("[{}] Handled {} code={} status={} path={} cid={} ",
                sessionId, name, exception.getCode(), exception.getHttpStatus(), request.getRequestURI(), MDC.get("correlationId"));

        return ResponseEntity.status(exception.getHttpStatus()).body(problemDetail);

        // add more details, if its a validation error
    }


    //handling auth exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(Exception exception, HttpServletRequest request) {
        ProblemDetail problemDetail = this.base(HttpStatus.FORBIDDEN, "access_denied", exception, request);
        problemDetail.setProperty("code", "FORBIDDEN");
        problemDetail.setProperty("message", "you do not have permission to perform this action");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    public ResponseEntity<ProblemDetail> handleUnauth(Exception exception, HttpServletRequest request) {
        ProblemDetail problemDetail = this.base(HttpStatus.UNAUTHORIZED, "unauthorised", exception, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);

    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnknown(Exception exception, HttpServletRequest request) {
        String cid = MDC.get("correlationId");
        String sessionId = request.getSession().getId();

        log.error("[{}] Unhandled exception at {} cid={} ", sessionId, request.getRequestURI(), cid, exception);

        ProblemDetail problemDetail = this.base(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error", exception, request);
        problemDetail.setProperty("code", "internal_server_error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }




}
