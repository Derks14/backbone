package backbone.models.records;

import lombok.Builder;

import java.util.List;

@Builder
public record ImplementationHighlights(
        List<String> apiDesign,
        String databaseSchemaOverview,
        List<String> backgroundJobs,
        String errorHandling
) { }
