package backbone.models.spotify;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CurrentlyPlaying {
    private Device device;

    @JsonProperty("repeat_state")
    private String repeatState;

    @JsonProperty("shuffle_state")
    private boolean shuffleState;

    private Context context;

    private Timestamp timestamp;

    @JsonProperty("progress_ms")
    private Timestamp progressMs;

    @JsonProperty("is_playing")
    private boolean isPlaying;

    @JsonProperty("item")
    private Item item;


    @JsonProperty("currently_playing_type")
    private String currentlyPlayingType;

    private Actions actions;
}
