package backbone.services;

import backbone.configs.BackboneException;
import backbone.dto.*;
import backbone.models.Category;
import backbone.models.Copy;
import backbone.models.Project;
import backbone.models.records.CopyStatus;
import backbone.repositories.ProjectRepository;
import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraintvalidators.RegexpURLValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CopyService copyService;

    public ProjectService(ProjectRepository repository, CopyService copyService) {
        this.projectRepository = repository;
        this.copyService = copyService;
    }


    public Res<List<Project>> fetchProjects(FetchProjectRequest request, String sessionId) {
        log.info("[{}] processing request to fetch projects ", sessionId);
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Page<Project> projectsInPages;

        try {
            if (Objects.isNull(request.getSearch())) {
                projectsInPages = projectRepository.findAll(pageRequest);
            }
            else {
                log.info("[{}] full text search for request processing - searching projects : {} ", sessionId, request.getSearch());
                TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(request.getSearch());

                Sort sort = Sort.by("score");
                pageRequest.withSort(sort);
                projectsInPages = projectRepository.findAllBy(criteria, pageRequest);
            }
        } catch (Exception e) {
            throw new BackboneException("fetch_from_store_error", HttpStatus.INTERNAL_SERVER_ERROR, "process to fetch projects from store failed", e.getCause());
        }

        PaginationMeta paginationMeta = PaginationMapper.from(projectsInPages);

        List<Project> projects = projectsInPages.getContent();

        return Res.<List<Project>>builder()
                .message("projects fetched successfully")
                .data(projects)
                .pagination(paginationMeta)
                .timestamp(Instant.now())
                .build();
    }


    public Project fetchProject(String id, String sessionId) {
        log.info("[{}] processing request to fetch single project ", sessionId);
        Project project = projectRepository.findById(id)
                .orElseThrow( () -> new BackboneException("", HttpStatus.NOT_FOUND, "project with id: %s ".formatted(id)));
        log.info("[{}] project with title: {}. found successfully ", sessionId, project.getTitle());
        return project;
    }

    public Project addProject(ProjectDto projectDto, String sessionId) {
        log.info("[{}] processing request to new project ", sessionId);

        Project newProject = new Project();
        newProject.setTitle(projectDto.getTitle());
        newProject.setStatus(CopyStatus.DRAFT);
        newProject.setDescription(projectDto.getDescription());
        newProject.setCategory(Category.PROJECT);

        try{
            newProject = projectRepository.insert(newProject);
        } catch (DuplicateKeyException duplicateKeyException) {
            log.error("[{}] duplicate key exception : {} ", sessionId, duplicateKeyException.getMessage());
            throw new BackboneException("", HttpStatus.CONFLICT, "copy with title already exists", duplicateKeyException.getCause());
        }

        log.info("[{}] new project: {}, created as draft. only admin can see what it looks",sessionId, projectDto.getTitle());
        return newProject;
    }


    public Project updateProject(Project newData, String id, String sessionId) {
        log.info("[{}] processing update request to update project ", sessionId);

        Project project = projectRepository.findById(id)
                .orElseThrow( () -> new BackboneException(" ", HttpStatus.NOT_FOUND, "project with id: s%, could not be found") );

        try {
            project.setTitle(newData.getTitle());
            project.setDescription(newData.getDescription());
            project.setTags(newData.getTags());
            project.setStatus(newData.getStatus());
            project.setHero(newData.getHero());
            project.setProblemStatement(newData.getProblemStatement());
            project.setSystemArchitecture(newData.getSystemArchitecture());
            project.setEngineeringDecision(newData.getEngineeringDecision());
            project.setImplementationHighlights(newData.getImplementationHighlights());
            project.setOutcomesAndLearnings(newData.getOutcomesAndLearnings());
            project.setCallToAction(newData.getCallToAction());

            project = projectRepository.save(project);
        } catch (Exception e) {
            throw new BackboneException("", HttpStatus.INTERNAL_SERVER_ERROR, "project update failed ", e.getCause());
        }

        log.info("[{}] project: {} , has been successfully updated", sessionId, project.getTitle());

        return project;
    }

    public Project getProject(String id, String sessionId) {
        log.info("[{}] processing request to get single project ", sessionId);

        Project project = projectRepository.findById(id)
                .orElseThrow( () -> new BackboneException("", HttpStatus.NOT_FOUND, "project with id: %s, could not be found".formatted(id) ));

        log.info("[{}] project found and returned ", sessionId);
        return project;
    }

    public Res deleteProject(String id, String sessionId) {
        log.info("[{}] processing request to delete project ", sessionId);

        Project project = projectRepository.findById(id)
                .orElseThrow( () -> new BackboneException("",HttpStatus.NOT_FOUND, "project with id: %s, could not be found".formatted(id) ) );

        // in the future, try to implement a better way of deleting these things
        projectRepository.delete(project);

        log.info("[{}] project deleted successfully", sessionId);
        return Res.builder().message("project details deleted successfyully").build();

    }
}
