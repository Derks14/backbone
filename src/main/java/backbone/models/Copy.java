package backbone.models;

import backbone.models.records.CopyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Copy extends BaseDocument{
    private Category category;
    private String title;
    private String description;
    private List<String> tags;
    private String icon;
    private CopyStatus status;
}
