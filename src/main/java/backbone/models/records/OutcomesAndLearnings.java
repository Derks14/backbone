package backbone.models.records;

import lombok.Builder;

import java.util.List;

@Builder
public record OutcomesAndLearnings(
        List<String> whatWorked,
        List<String> whatBroke,
        List<String> futureImprovements
) { }
