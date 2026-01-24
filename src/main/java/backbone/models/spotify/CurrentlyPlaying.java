package backbone.models.spotify;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


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

    private int timestamp;

    @JsonProperty("progress_ms")
    private int progressMs;

    @JsonProperty("is_playing")
    private boolean isPlaying;

    private Item item;

    @JsonProperty("currently_playing_type")
    private String currentlyPlayingType;

    private Actions actions;
}
