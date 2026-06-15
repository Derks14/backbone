package backbone.models;


import backbone.models.enums.ContentFormat;
import backbone.models.enums.DetailType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
@EqualsAndHashCode(callSuper = true)
public class Detail extends BaseDocument {
    private String title;
    private String subtitle;
    private DetailType type;
    private DetailStatus status;
    private Relevance relevance;
    private ContentFormat contentFormat; // markdown, mdx, json_blocks
    private String content;
}
