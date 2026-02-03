package backbone.models.records;

import lombok.Builder;

@Builder
public record Visibility(boolean isPublic, boolean isFeatured) {
}
