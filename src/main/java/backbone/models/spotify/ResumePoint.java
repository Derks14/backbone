package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ResumePoint {

    @JsonProperty("fully_played")
    private boolean fullyPlayed;

    @JsonProperty("resume_positon_ms")
    private boolean resumePositionMs;
}
