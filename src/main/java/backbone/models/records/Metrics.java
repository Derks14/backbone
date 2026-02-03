package backbone.models.records;

import lombok.Builder;

@Builder
public record Metrics(int latencyMs, long requestsPerSecond, double uptimePercent) { }
