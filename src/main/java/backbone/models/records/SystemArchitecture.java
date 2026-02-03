package backbone.models.records;

import lombok.Builder;

import java.util.List;

@Builder
public record SystemArchitecture(
        String diagramUrl,
        List<String> dataFlow,
        String designRationale
) { }
