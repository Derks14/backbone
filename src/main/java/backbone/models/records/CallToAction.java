package backbone.models.records;

import lombok.Builder;

@Builder
public record CallToAction(
        String label,
        String url
) { }
