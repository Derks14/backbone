package backbone.models.spotify;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public
class Context {
    private String type;
    private String href;
    private String uri;
}


