package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Episode extends Item {

    private String description;

    private String html_description;

    private List<Image> images;

    private boolean isExternallyHosted;

    private boolean isPlayable;

    private List<String> languages;

    private String name;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("release_date_precision")
    private String releaseDatePrecision;

    private ResumePoint resumePoint;

    private Show show;
}
