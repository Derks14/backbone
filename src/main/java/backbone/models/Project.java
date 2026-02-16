package backbone.models;

import backbone.models.records.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Project extends Copy {
    Hero hero;
    ProblemStatement problemStatement;
    SystemArchitecture systemArchitecture;
    List<EngineeringDecision> engineeringDecision;
    ImplementationHighlights implementationHighlights;
    OutcomesAndLearnings outcomesAndLearnings;
    Action callToAction;
}


