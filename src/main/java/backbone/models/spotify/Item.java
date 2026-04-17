package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.time.Duration;
import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Track.class, name= "track" ),
        @JsonSubTypes.Type(value = Episode.class,  name ="episode")
})
public class Item {
    private String id;
    private String name;
    private String type;

    private String uri;

    private Restrictions restrictions;

    private String href;

    @JsonProperty("external_urls")
    private ExternalUrls externalUrls;

    private boolean explicit;

    @JsonProperty("duration_ms")
    private Long durationMs;


}
