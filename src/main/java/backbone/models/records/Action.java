package backbone.models.records;

import lombok.Builder;

import java.util.List;

@Builder
public record Action(
        CallToAction primary,
        List<CallToAction> secondary
) { }
