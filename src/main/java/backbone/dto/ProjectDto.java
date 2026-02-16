package backbone.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProjectDto {
    String title;
    String description;
}
