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
    Category category;

    @NotBlank
    String title;

    @NotBlank
    String description;

    @NotEmpty
    List<String> tags;

    @NotBlank
    String icon;

    @NotBlank
    CopyStatus status;
}
