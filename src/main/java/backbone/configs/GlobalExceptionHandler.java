package backbone.configs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ProblemDetail base(
            HttpStatus status,
            String title,
            Exception exception,
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
    public ResponseEntity<ProblemDetail> handleBackboneExceptions(BackboneException exception, HttpServletRequest request) {
        String name = exception.getClass().getSimpleName();
        String sessionId = request.getSession().getId();

        ProblemDetail problemDetail = this.base(exception.getHttpStatus(), exception.getCode(), exception, request);
        problemDetail.setProperty("code", exception.getCode());

        log.error("[{}] Handled {} code={} status={} path={} cid={} ",
                sessionId, name, exception.getCode(), exception.getHttpStatus(), request.getRequestURI(), MDC.get("correlationId"));

        return ResponseEntity.status(exception.getHttpStatus()).body(problemDetail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(Exception exception, HttpServletRequest request) {
        ProblemDetail problemDetail = this.base(HttpStatus.FORBIDDEN, "access_denied", exception, request);
        problemDetail.setProperty("code", "FORBIDDEN");
        problemDetail.setProperty("message", "you do not have permission to perform this action");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            BindException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception exception, HttpServletRequest request) {
        ProblemDetail problemDetail = this.base(HttpStatus.BAD_REQUEST, "bad request", exception, request);
        problemDetail.setProperty("code", "bad_request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
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
