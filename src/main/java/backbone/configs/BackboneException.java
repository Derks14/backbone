package backbone.configs;

import lombok.*;
import org.springframework.http.HttpStatus;


@Getter
public class BackboneException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;


    public BackboneException(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BackboneException(String code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BackboneException(String code, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }


}
