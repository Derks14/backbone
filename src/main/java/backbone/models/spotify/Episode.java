package backbone.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Episode extends Item {

    @JsonProperty("audio_preview_url")
    private String audioPreviewUrl;

    private String description;

    @JsonProperty("html_description")
    private String html_description;

    private List<Image> images;

    @JsonProperty("is_externally_hosted")
    private boolean isExternallyHosted;

    @JsonProperty("is_playable")
    private boolean isPlayable;

    private List<String> languages;

    private String name;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("release_date_precision")
    private String releaseDatePrecision;

    @JsonProperty("resume_point")
    private ResumePoint resumePoint;

    private Show show;
}
