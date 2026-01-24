package backbone.models.spotify;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Device {
    private String id;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("is_private_session")
    private boolean isPrivateSession;

    @JsonProperty("is_restricted")
    private boolean isRestricted;

    private String name;

    private String type;

    @JsonProperty("volume_percent")
    private int volumePercent;

    @JsonProperty("supports_volume")
    private boolean supportsVolume;
}
