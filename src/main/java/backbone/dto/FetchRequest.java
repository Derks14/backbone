package backbone.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FetchRequest {
    private String search;
    private int page;
    private int size;
    private String direction;
}
