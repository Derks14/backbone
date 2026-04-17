package backbone.models.spotify;


import backbone.models.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;


@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SpotifyToken extends BaseDocument {
    private String accessToken;
    private String tokenType;
    private String scope;
    private int expiresIn;
    private Instant accessTokenExpiresAt;

    @Indexed
    private String refreshToken;
}
