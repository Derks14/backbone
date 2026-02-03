package backbone.models;

import backbone.models.records.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Builder
@AllArgsConstructor
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


