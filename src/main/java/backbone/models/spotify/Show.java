package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    @JsonProperty("available_markets")
    private List<String> availableMarkets;
    private String description;

    @JsonProperty("html_description")
    private String htmlDescription;

    private String publisher;

    private String name;

    private String type;

    private String uri;

    @JsonProperty("total_episodes")
    private int totalEpisodes;
}
