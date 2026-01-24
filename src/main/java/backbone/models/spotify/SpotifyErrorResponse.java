package backbone.models.spotify;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class SpotifyErrorResponse {
    private int status;
    private String message;
}
