package backbone.models;

import backbone.models.records.CopyStatus;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Copy extends BaseDocument{
    private Category category;

    @Indexed(unique = true)
    private String title;

    private String description;

    private List<String> tags;

    private String icon;

    private CopyStatus status;
}
