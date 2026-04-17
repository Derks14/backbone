package backbone.models.spotify;


import backbone.models.BaseDocument;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Data
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyAuthorisationToken {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    private String scope;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
