package backbone.services;

import backbone.configs.BackboneException;
import backbone.dto.FetchProjectRequest;
import backbone.dto.ProjectDto;
import backbone.dto.Res;
import backbone.models.Category;
import backbone.models.Project;
import backbone.models.records.CopyStatus;
import backbone.repositories.ProjectRepository;
import com.mongodb.DuplicateKeyException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcernResult;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CopyService copyService;

    @InjectMocks
    private ProjectService projectService;

    @Captor
    private ArgumentCaptor<Project> projectCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Captor
    private ArgumentCaptor<TextCriteria> textCriteriaCaptor;

    private static final String SESSION_ID = "session-123";

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, copyService);
    }

    @Test
    void fetchProjectsShouldReturnPagedDataWhenSearchIsNull() {
        FetchProjectRequest request = FetchProjectRequest.builder().page(0).size(2).search(null).build();
        Project project = projectWithIdAndTitle("p-1", "Project One");

        Page<Project> page = new PageImpl<>(List.of(project), PageRequest.of(0, 2), 3);
        when(projectRepository.findAll(eq(PageRequest.of(0, 2)))).thenReturn(page);

        Res<List<Project>> response = projectService.fetchProjects(request, SESSION_ID);

        assertThat(response.getMessage()).isEqualTo("projects fetched successfully");
        assertThat(response.getData()).containsExactly(project);
        assertThat(response.getPagination().getPage()).isEqualTo(0);
        assertThat(response.getPagination().getSize()).isEqualTo(2);
        assertThat(response.getPagination().getTotalElements()).isEqualTo(3);
        assertThat(response.getPagination().getTotalPages()).isEqualTo(2);
        assertThat(response.getPagination().isHasNext()).isTrue();
        assertThat(response.getPagination().isHasPrevious()).isFalse();

        verify(projectRepository).findAll(PageRequest.of(0, 2));
        verify(projectRepository, never()).findAllBy(any(TextCriteria.class), any(Pageable.class));
    }

    @Test
    void fetchProjectsShouldUseTextSearchAndScoreSortingWhenSearchProvided() {
        FetchProjectRequest request = FetchProjectRequest.builder().page(1).size(3).search("gen ai").build();
        Project project = projectWithIdAndTitle("p-2", "AI Project");

        Page<Project> page = new PageImpl<>(List.of(project), PageRequest.of(1, 3), 4);
        when(projectRepository.findAllBy(any(TextCriteria.class), any(Pageable.class))).thenReturn(page);

        Res<List<Project>> response = projectService.fetchProjects(request, SESSION_ID);

        assertThat(response.getData()).containsExactly(project);
        assertThat(response.getPagination().getPage()).isEqualTo(1);
        assertThat(response.getPagination().isHasNext()).isFalse();
        assertThat(response.getPagination().isHasPrevious()).isTrue();

        verify(projectRepository).findAllBy(textCriteriaCaptor.capture(), pageableCaptor.capture());
        assertThat(textCriteriaCaptor.getValue()).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("score")).isNotNull();
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);
    }

    @Test
    void fetchProjectsShouldThrowBackboneExceptionWhenRepositoryFails() {
        FetchProjectRequest request = FetchProjectRequest.builder().page(0).size(5).build();
        when(projectRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("store down"));

        assertThatThrownBy(() -> projectService.fetchProjects(request, SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> {
                    BackboneException be = (BackboneException) ex;
                    assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(be.getCode()).isEqualTo("fetch_from_store_error");
                    assertThat(be.getMessage()).isEqualTo("process to fetch projects from store failed");
                });
    }

    @Test
    void fetchProjectShouldReturnProjectWhenItExists() {
        Project project = projectWithIdAndTitle("p-10", "Existing Project");
        when(projectRepository.findById("p-10")).thenReturn(Optional.of(project));

        Project response = projectService.fetchProject("p-10", SESSION_ID);

        assertThat(response).isEqualTo(project);
    }

    @Test
    void fetchProjectShouldThrowNotFoundWhenProjectMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.fetchProject("missing", SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> assertThat(((BackboneException) ex).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void addProjectShouldCreateDraftProjectAndInsertIt() {
        ProjectDto dto = ProjectDto.builder().title("New Project").description("A description").build();
        when(projectRepository.insert(any(Project.class))).thenAnswer(invocation -> {
            Project inserted = invocation.getArgument(0, Project.class);
            inserted.setId("new-id");
            return inserted;
        });

        Project response = projectService.addProject(dto, SESSION_ID);

        verify(projectRepository).insert(projectCaptor.capture());
        Project inserted = projectCaptor.getValue();

        assertThat(inserted.getTitle()).isEqualTo("New Project");
        assertThat(inserted.getDescription()).isEqualTo("A description");
        assertThat(inserted.getStatus()).isEqualTo(CopyStatus.DRAFT);
        assertThat(inserted.getCategory()).isEqualTo(Category.PROJECT);

        assertThat(response.getId()).isEqualTo("new-id");
        assertThat(response.getTitle()).isEqualTo("New Project");
    }

    @Test
    void addProjectShouldThrowConflictWhenTitleAlreadyExists() {
        ProjectDto dto = ProjectDto.builder().title("Duplicate").description("desc").build();
        DuplicateKeyException duplicate = new DuplicateKeyException(
                new BsonDocument(),
                new ServerAddress(),
                WriteConcernResult.acknowledged(0, false, null)
        );

        when(projectRepository.insert(any(Project.class))).thenThrow(duplicate);

        assertThatThrownBy(() -> projectService.addProject(dto, SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> {
                    BackboneException be = (BackboneException) ex;
                    assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(be.getMessage()).isEqualTo("copy with title already exists");
                });
    }

    @Test
    void updateProjectShouldMergeIncomingDataAndPersist() {
        Project existing = projectWithIdAndTitle("p-20", "Old");
        existing.setDescription("old desc");
        existing.setStatus(CopyStatus.DRAFT);

        Project updates = new Project();
        updates.setTitle("New Title");
        updates.setDescription("New Description");
        updates.setTags(List.of("spring", "mongo"));
        updates.setStatus(CopyStatus.PUBLISHED);

        when(projectRepository.findById("p-20")).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0, Project.class));

        Project response = projectService.updateProject(updates, "p-20", SESSION_ID);

        verify(projectRepository).save(projectCaptor.capture());
        Project saved = projectCaptor.getValue();

        assertThat(saved.getId()).isEqualTo("p-20");
        assertThat(saved.getTitle()).isEqualTo("New Title");
        assertThat(saved.getDescription()).isEqualTo("New Description");
        assertThat(saved.getTags()).containsExactly("spring", "mongo");
        assertThat(saved.getStatus()).isEqualTo(CopyStatus.PUBLISHED);
        assertThat(response).isEqualTo(saved);
    }

    @Test
    void updateProjectShouldThrowNotFoundWhenProjectMissing() {
        when(projectRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(new Project(), "unknown", SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> assertThat(((BackboneException) ex).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateProjectShouldThrowInternalServerErrorWhenSaveFails() {
        Project existing = projectWithIdAndTitle("p-30", "Existing");
        Project updates = new Project();
        updates.setTitle("Updated");

        when(projectRepository.findById("p-30")).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenThrow(new RuntimeException("write failure"));

        assertThatThrownBy(() -> projectService.updateProject(updates, "p-30", SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> {
                    BackboneException be = (BackboneException) ex;
                    assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(be.getMessage()).isEqualTo("project update failed ");
                });
    }

    @Test
    void getProjectShouldReturnProjectWhenFound() {
        Project existing = projectWithIdAndTitle("p-40", "Readable Project");
        when(projectRepository.findById("p-40")).thenReturn(Optional.of(existing));

        Project response = projectService.getProject("p-40", SESSION_ID);

        assertThat(response).isEqualTo(existing);
    }

    @Test
    void getProjectShouldThrowNotFoundWhenMissing() {
        when(projectRepository.findById("missing-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject("missing-2", SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> assertThat(((BackboneException) ex).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteProjectShouldDeleteAndReturnSuccessMessage() {
        Project existing = projectWithIdAndTitle("p-50", "Delete Me");
        when(projectRepository.findById("p-50")).thenReturn(Optional.of(existing));

        Res<?> response = projectService.deleteProject("p-50", SESSION_ID);

        verify(projectRepository).delete(existing);
        assertThat(response.getMessage()).isEqualTo("project details deleted successfyully");
    }

    @Test
    void deleteProjectShouldThrowNotFoundWhenProjectMissing() {
        when(projectRepository.findById("missing-3")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject("missing-3", SESSION_ID))
                .isInstanceOf(BackboneException.class)
                .satisfies(ex -> assertThat(((BackboneException) ex).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Project projectWithIdAndTitle(String id, String title) {
        Project project = new Project();
        project.setId(id);
        project.setTitle(title);
        return project;
    }
}
