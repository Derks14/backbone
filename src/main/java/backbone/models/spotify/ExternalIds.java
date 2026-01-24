package backbone.models.spotify;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ExternalIds {
    private String isrc;
    private String ean;
    private String upc;
}
