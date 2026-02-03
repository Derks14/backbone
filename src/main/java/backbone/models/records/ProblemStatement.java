package backbone.models.records;

import lombok.Builder;

import java.util.List;

@Builder
public record ProblemStatement(
        String problem,
        String realWorldMotivation,
        List<String> constraints,
        String whyExistingSolutionsFailed
) { }
