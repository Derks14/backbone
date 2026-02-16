package backbone.dto;

import backbone.models.Category;
import backbone.models.records.CopyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

import java.util.List;

@Value
public class CopyDto {
    @NotBlank
    private Category category;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    private List<String> tags;

    @NotBlank
    private String icon;

    @NotBlank
    private CopyStatus status;
}
