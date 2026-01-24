package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Album {

    @JsonProperty("album_type")
    private String albumType;

    @JsonProperty("total_tracks")
    private int totalTracks;

    private List<String> available_markets;

    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;

    private List<Image> images;

    private String name;

    private String releaseDate;

    private String releaseDatePrecision;

    private Restrictions restrictions;

    private String type;

    private String uri;

    private List<Artist> artists;

}
