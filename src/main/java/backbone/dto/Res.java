package backbone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Res<T> {
    private String message;
    private T data;
    private PaginationMeta pagination;
    private Instant timestamp;
    private String path;

    public Res(String message, T data, PaginationMeta pagination, String path) {
        this.message = message;
        this.data = data;
        this.pagination = pagination;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public Res(String message, T data,  String path) {
        this.message = message;
        this.data = data;
        this.path = path;
        this.timestamp = Instant.now();
    }

}
