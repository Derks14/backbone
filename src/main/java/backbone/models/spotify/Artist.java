package backbone.models.spotify;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Artist {

    private String href;
    private String id;
    private  String name;
    private String type;
    private String uri;
}
