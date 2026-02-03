package backbone.models.records;

import lombok.Builder;

import java.util.List;

@Builder
public record Hero(
        String projectName,
        String valueStatement,
        List<String> techStack,
        Links links
        ) {

    public record Links (String github, String liveDemo) { }
}