package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Item {
    private String type;

    private String uri;

    private Restrictions restrictions;

    private String name;


    private String href;

    private String id;

    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;

    private boolean explicit;

    @JsonProperty("duration_ms")
    private int durationMs;
}
