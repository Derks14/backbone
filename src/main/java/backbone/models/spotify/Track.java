package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
@Setter
public class Track extends Item {
    private Album album;
    private List<Artist> artists;

    @JsonProperty("available_markets")
    private List<String> availableMarkets;

    @JsonProperty("disc_number")
    private int discNumber;

    @JsonProperty("external_ids")
    private ExternalIds externalIds;

    @JsonProperty("is_playable")
    private boolean is_playable;

    private int popularity;

    @JsonProperty("track_number")
    private String track_number;


    @JsonProperty("is_local")
    private boolean isLocal;

 }
