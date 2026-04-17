package backbone.models.spotify;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DataRequest<T> {
    private String status;
    private T data;
}
