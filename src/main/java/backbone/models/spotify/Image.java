package backbone.models.spotify;


import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private String url;
    private int height;
    private int width;
}
