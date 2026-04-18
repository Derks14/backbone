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

    @JsonProperty("available_markets")
    @Getter(AccessLevel.NONE)
    private List<String> availableMarkets;

    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;

    private List<Image> images;

    private String name;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("release_date_precision")
    private String releaseDatePrecision;

    private Restrictions restrictions;

    private String type;

    private String uri;

    private List<Artist> artists;

}
