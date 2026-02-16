package backbone.controllers;


import backbone.dto.FetchProjectRequest;
import backbone.dto.ProjectDto;
import backbone.dto.Res;
import backbone.models.Copy;
import backbone.models.Project;
import backbone.services.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<Res<List<Project>>> fetch(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page,
            HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to fetch projects", sessionId);

        FetchProjectRequest projectRequest = FetchProjectRequest.builder()
                .page(page)
                .size(size)
                .search(search)
                .build();

        Res<List<Project>> response =projectService.fetchProjects(projectRequest, sessionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<Res<Project>> fetchProject(@PathVariable String id, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to fetch single project ", sessionId);

        Project project = projectService.fetchProject(id, sessionId);
        Res<Project> response = new Res<>("single project fetched successfully", project, request.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Res<Project>> createProject(@RequestBody ProjectDto projectDto, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] new request to add new project", sessionId);

        Project project = projectService.addProject(projectDto, sessionId);
        Res<Project> response = new Res<>("project draft created successfully", project, request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<Res<Project>> updateProject(@RequestBody Project project,
                                                      @PathVariable String id,
                                                      HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to update project ", sessionId);

        Project updatedProject = this.projectService.updateProject(project, id, sessionId);

        Res<Project> response = Res.<Project>builder()
                .message("project updated successfully")
                .data(updatedProject)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Res> deleteMapping(@PathVariable String id, HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        log.info("[{}] request to delete project ", sessionId);
        Res response = this.projectService.deleteProject(id, sessionId);
        return ResponseEntity.ok(response);
    }
}
