package backbone.repositories;

import backbone.models.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryTest {

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void crudOperationsShouldReadWriteAndDeleteProjectsThroughRepositoryContract() {
        Project newProject = new Project();
        newProject.setTitle("Project Alpha");

        Project savedProject = new Project();
        savedProject.setId("p-1");
        savedProject.setTitle("Project Alpha");

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectRepository.findById("p-1")).thenReturn(Optional.of(savedProject));
        when(projectRepository.findAll()).thenReturn(List.of(savedProject));
        doNothing().when(projectRepository).delete(savedProject);

        Project persisted = projectRepository.save(newProject);
        Optional<Project> fetched = projectRepository.findById("p-1");
        List<Project> allProjects = projectRepository.findAll();
        projectRepository.delete(savedProject);

        assertThat(persisted.getId()).isEqualTo("p-1");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getTitle()).isEqualTo("Project Alpha");
        assertThat(allProjects).hasSize(1);

        verify(projectRepository).save(newProject);
        verify(projectRepository).findById("p-1");
        verify(projectRepository).findAll();
        verify(projectRepository).delete(savedProject);
    }

    @Test
    void findAllByShouldReturnPagedProjectsForTextSearchCriteria() {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny("ai");
        PageRequest pageable = PageRequest.of(0, 2);

        Project project = new Project();
        project.setId("p-2");
        project.setTitle("AI Assistant");

        Page<Project> expectedPage = new PageImpl<>(List.of(project), pageable, 1);
        when(projectRepository.findAllBy(eq(criteria), eq(pageable))).thenReturn(expectedPage);

        Page<Project> result = projectRepository.findAllBy(criteria, pageable);

        assertThat(result.getContent()).containsExactly(project);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);

        verify(projectRepository).findAllBy(criteria, pageable);
    }

    @Test
    void repositoryShouldPropagateExceptionsForCrudAndQueryFailures() {
        Project project = new Project();
        project.setTitle("Failure Case");

        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny("failure");
        PageRequest pageable = PageRequest.of(0, 10);

        when(projectRepository.save(any(Project.class))).thenThrow(new RuntimeException("write failed"));
        when(projectRepository.findAllBy(eq(criteria), eq(pageable))).thenThrow(new RuntimeException("query failed"));

        assertThatThrownBy(() -> projectRepository.save(project))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("write failed");

        assertThatThrownBy(() -> projectRepository.findAllBy(criteria, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("query failed");
    }
}
