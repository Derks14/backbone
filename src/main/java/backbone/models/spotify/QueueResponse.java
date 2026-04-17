package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Data
public class QueueResponse {
    @JsonProperty("currently_playing")
    private Item currentlyPlaying;

    @JsonProperty("queue")
    private List<Item> queue;
}
